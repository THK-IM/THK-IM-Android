package com.thk.im.android.core.module.internal

import com.google.gson.Gson
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.api.ApiManager
import com.thk.im.android.core.api.BaseSubscriber
import com.thk.im.android.core.api.MessageApi
import com.thk.im.android.core.api.RxTransform
import com.thk.im.android.core.api.SessionApi
import com.thk.im.android.core.bean.AckMsgBean
import com.thk.im.android.core.bean.CreateSessionBean
import com.thk.im.android.core.bean.DeleteMsgBean
import com.thk.im.android.core.bean.MessageBean
import com.thk.im.android.core.bean.SessionBean
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.core.utils.LLog
import com.thk.im.android.db.dao.SessionType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import kotlin.math.abs

/**
 * 内部默认的消息处理器
 */
open class DefaultMessageModule : MessageModule {

    private var lastTimestamp = 0L
    private var lastSequence = 0

    override fun onSignalReceived(subType: Int, body: String) {
        val messageBean = Gson().fromJson(body, MessageBean::class.java)
        onNewMessage(messageBean)
    }

    override fun newMsgId(): Long {
        synchronized(this) {
            val current = IMManager.getSignalModule().severTime
            if (current == lastTimestamp) {
                lastSequence++
            } else {
                lastTimestamp = current
                lastSequence = 0
            }
            return current * 100 + lastSequence
        }
    }

    override fun getMessageProcessor(messageType: Int): BaseMsgProcessor {
        return IMManager.getMsgProcessor(messageType)
    }

    private val needAckMap = HashMap<Long, MutableList<Long>>()
    private var lastAckTime = 0L

    override fun ackMessage(sid: Long, msgId: Long) {
        LLog.v("ackMessage", "$sid, msgId: $msgId")
        synchronized(this) {
            if (needAckMap[sid] != null) {
                needAckMap[sid]!!.add(msgId)
            } else {
                needAckMap[sid] = mutableListOf(msgId)
            }
        }

        // 间隔10s发送ack
        val now = IMManager.getSignalModule().severTime
        if (abs(now - lastAckTime) > 10 * 1000) {
            ackMessages()
        }
    }

    override fun ackMessages() {
        synchronized(this) {
            needAckMap.forEach {
                ackMessages(it.key, it.value)
            }
            needAckMap.clear()
            lastAckTime = IMManager.getSignalModule().severTime
        }
    }

    override fun ackMessages(sid: Long, msgIds: List<Long>) {
        val body = AckMsgBean(sid, IMManager.getUid(), msgIds)
        ApiManager.getImApi(MessageApi::class.java)
            .ackMsg(body)
            .subscribe(object : BaseSubscriber<Void>() {
                override fun onNext(t: Void?) {
                    dispose()
                }
            })
    }

    override fun deleteServerMessages(sid: Long, msgIds: List<Long>): Flowable<Void> {
        val uid = IMManager.getUid()
        val reqBean = DeleteMsgBean(sid, uid, msgIds)
        return ApiManager.getImApi(MessageApi::class.java).deleteMessages(reqBean)
    }

    override fun deleteMessages(
        sid: Long,
        messages: List<Message>,
        deleteServer: Boolean
    ): Flowable<Boolean> {
        if (!deleteServer) {
            return Flowable.create({
                try {
                    for (msg in messages) {
                        getMessageProcessor(msg.type).deleteMessage(msg)
                    }
                    it.onNext(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.onNext(false)
                }
            }, BackpressureStrategy.LATEST)
        } else {
            val msgIds = mutableListOf<Long>()
            for (msg in messages) {
                msgIds.add(msg.msgId)
            }
            return deleteServerMessages(sid, msgIds).flatMap {
                try {
                    for (msg in messages) {
                        getMessageProcessor(msg.type).deleteMessage(msg)
                    }
                    Flowable.just(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Flowable.just(false)
                }
            }
        }
    }

    override fun onNewMessage(bean: MessageBean) {
        getMessageProcessor(bean.type).received(bean)
    }

    override fun sendMessageToServer(bean: MessageBean): Flowable<MessageBean> {
        return ApiManager.getImApi(MessageApi::class.java).sendMsg(bean).flatMap {
            // 服务端返回的msgId合并到新对象中
            val finalBean = MessageBean(
                bean.clientId, bean.fUId, bean.sessionId, it.msgId, bean.type,
                bean.body, bean.atUsers, bean.rMsgId, it.cTime
            )
            return@flatMap Flowable.just(finalBean)
        }
    }

    override fun syncLatestMessagesFromServer(
        cTime: Long,
        offset: Int,
        size: Int
    ): Flowable<List<MessageBean>> {
        return ApiManager.getImApi(MessageApi::class.java).queryLatestMsg(
            IMManager.getUid(),
            cTime, offset, size
        ).flatMap {
            Flowable.just(it.data)
        }
    }

    override fun syncOfflineMessages(cTime: Long, offset: Int, size: Int) {
        syncLatestMessagesFromServer(cTime, offset, size)
            .compose(RxTransform.flowableToIo())
            .subscribe(object : BaseSubscriber<List<MessageBean>>() {
                override fun onNext(t: List<MessageBean>) {
                    for (bean in t) {
                        onNewMessage(bean)
                    }
                    if (t.size >= size) {
                        syncOfflineMessages(cTime, offset + t.size, size)
                    }
                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                    t?.printStackTrace()
                }
            })

    }

    override fun syncAllMessages(offset: Int, size: Int): Flowable<List<MessageBean>> {
        val cTime = 0L
        return ApiManager.getImApi(MessageApi::class.java).queryLatestMsg(
            IMManager.getUid(),
            cTime, offset, size
        ).flatMap {
            Flowable.just(it.data)
        }
    }

    override fun syncLatestSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>> {
        val mTime = IMManager.getSignalModule().severTime
        return ApiManager.getImApi(SessionApi::class.java).queryLatestSession(
            IMManager.getUid(),
            mTime, offset, size
        )
    }

    override fun getSession(uid: Long, map: Map<String, Any>): Flowable<Session> {
        return Flowable.create({
            var session = IMManager.getImDataBase()
                .sessionDao().findSessionByEntity(uid, SessionType.Single.value)
            if (session == null) {
                session = Session(SessionType.Single.value, uid)
            }
            it.onNext(session)
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.id == 0L) {
                return@flatMap getSessionFromServerByEntityId(
                    it.entityId,
                    it.type,
                    map
                ).flatMap { bean ->
                    val session = bean.toSession()
                    IMManager.getImDataBase().sessionDao().insertSessions(session)
                    Flowable.just(session)
                }
            }
            return@flatMap Flowable.just(it)
        }.compose(RxTransform.flowableToMain())
    }

    override fun getSessionFromServerByEntityId(
        entityId: Long,
        type: Int,
        map: Map<String, Any>
    ): Flowable<SessionBean> {
        val selfUid = IMManager.getUid()
        val members = arrayListOf(selfUid, entityId)
        val bean = CreateSessionBean(type, null, members)
        return ApiManager.getImApi(SessionApi::class.java).createSession(bean)
    }

    override fun querySessionFromServer(sid: Long): Flowable<SessionBean> {
        val selfUid = IMManager.getUid()
        return ApiManager.getImApi(SessionApi::class.java).querySession(selfUid, sid)
    }

    override fun queryLocalSession(sessionId: Long): Flowable<Session> {
        return Flowable.create({
            var session = IMManager.getImDataBase()
                .sessionDao().findSession(sessionId)
            if (session == null) {
                session = Session(sessionId)
            }
            it.onNext(session)
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.entityId == 0L && it.type == 0) {
                return@flatMap querySessionFromServer(
                    sessionId
                ).flatMap { bean ->
                    val session = bean.toSession()
                    IMManager.getImDataBase().sessionDao().insertSessions(session)
                    Flowable.just(session)
                }
            }
            return@flatMap Flowable.just(it)
        }.compose(RxTransform.flowableToMain())
    }

    override fun syncAllSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>> {
        val mTime = 0L
        return ApiManager.getImApi(SessionApi::class.java).queryLatestSession(
            IMManager.getUid(),
            mTime, offset, size
        )
    }

    override fun queryLocalSessions(offset: Int, size: Int): Flowable<List<Session>> {
        return Flowable.create(
            {
                val sessions =
                    IMManager.getImDataBase().sessionDao().querySessionsByMTime(offset, size)
                it.onNext(sessions)
            }, BackpressureStrategy.LATEST
        ).compose(RxTransform.flowableToMain())
    }

    override fun queryLocalMessages(
        sessionId: Long,
        cTime: Long,
        size: Int
    ): Flowable<List<Message>> {
        return Flowable.create(
            {
                val messages =
                    IMManager.getImDataBase().messageDao()
                        .queryMessagesBySidAndCTime(sessionId, cTime, size)
                it.onNext(messages)
            }, BackpressureStrategy.LATEST
        ).compose(RxTransform.flowableToMain())
    }

    override fun deleteServerSession(sessionList: List<Session>): Flowable<Int> {
        return Flowable.just(0)
    }

    override fun deleteLocalSession(sessionList: List<Session>): Flowable<Int> {
        return Flowable.create(FlowableOnSubscribe {
            val result = IMManager.getImDataBase().sessionDao()
                .deleteSessions(*sessionList.toTypedArray())
            it.onNext(result)
        }, BackpressureStrategy.LATEST)
    }

    override fun deleteSession(
        sessionList: List<Session>,
        deleteServer: Boolean
    ): Flowable<Boolean> {
        return if (deleteServer) {
            deleteServerSession(sessionList).flatMap {
                deleteLocalSession(sessionList)
            }.map {
                true
            }
        } else {
            deleteLocalSession(sessionList).map {
                true
            }
        }
    }

    override fun signMessageReadBySessionId(sessionId: Long) {
        IMManager.getImDataBase().messageDao().updateMessagesRead(sessionId)
    }
}