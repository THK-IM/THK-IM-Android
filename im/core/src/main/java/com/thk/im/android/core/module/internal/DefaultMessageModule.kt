package com.thk.im.android.core.module.internal

import com.google.gson.Gson
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.bean.MessageBean
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.db.SessionType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

/**
 * 内部默认的消息处理器
 */
open class DefaultMessageModule : MessageModule {

    private val processorMap: MutableMap<Int, BaseMsgProcessor> = HashMap()
    private var lastTimestamp: Long = 0
    private var lastSequence: Int = 0
    private val needAckMap = HashMap<Long, MutableSet<Long>>()
    private var lastAckTime = 0L
    private val disposes = CompositeDisposable()

    override fun registerMsgProcessor(processor: BaseMsgProcessor) {
        processorMap[processor.messageType()] = processor
    }

    override fun getMsgProcessor(msgType: Int): BaseMsgProcessor {
        val processor = processorMap[msgType]
        return processor ?: processorMap[0]!!
    }

    override fun syncOfflineMessages() {
        val lastTime = this.getOfflineMsgLastSyncTime()
        val count = 200
        val disposable = object : BaseSubscriber<List<Message>>() {
            override fun onNext(t: List<Message>) {

            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.printStackTrace()
            }
        }
        IMCoreManager.getIMApi().getLatestMessages(IMCoreManager.getUid(), lastTime, count)
            .compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposes.add(disposable)
    }

    override fun syncLatestSessionsFromServer(lastSyncTime: Int, count: Int) {

    }

    override fun createSession(entityId: Long, sessionType: Int): Flowable<Session> {
        return Flowable.create<Session>({
            val session = IMCoreManager.getImDataBase().sessionDao()
                .findSessionByEntity(entityId, sessionType)
            if (session == null) {
                it.onNext(Session(sessionType, entityId))
            } else {
                it.onNext(session)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap { session ->
            if (session.id > 0) {
                return@flatMap Flowable.just(session)
            } else {
                val members = mutableSetOf(IMCoreManager.getUid())
                if (sessionType == SessionType.Single.value) {
                    members.add(entityId)
                }
                return@flatMap IMCoreManager.getIMApi()
                    .createSession(sessionType, entityId, members)
            }
        }
    }

    override fun getSession(sessionId: Long): Flowable<Session> {
        return Flowable.create<Session>({
            val session = IMCoreManager.getImDataBase().sessionDao().findSession(sessionId)
            if (session == null) {
                it.onNext(Session(0))
            } else {
                it.onNext(session)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap { session ->
            if (session.id > 0) {
                return@flatMap Flowable.just(session)
            } else {
                val uId = IMCoreManager.getUid()
                return@flatMap IMCoreManager.getIMApi().querySession(uId, sessionId)
            }
        }
    }

    override fun queryLocalSessions(count: Int, mTime: Long): Flowable<List<Session>> {
        return Flowable.create({
            val sessions = IMCoreManager.getImDataBase().sessionDao().querySessions(count, mTime)
            it.onNext(sessions)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun queryLocalMessages(
        sessionId: Long,
        cTime: Long,
        count: Int
    ): Flowable<List<Message>> {
        return Flowable.create({
            val sessions = IMCoreManager.getImDataBase().messageDao()
                .queryMessagesBySidAndCTime(sessionId, cTime, count)
            it.onNext(sessions)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun deleteSession(
        sessionList: Array<Session>,
        deleteServer: Boolean
    ): Flowable<Boolean> {
        TODO("Not yet implemented")
    }

    override fun onNewMessage(msg: Message) {
        TODO("Not yet implemented")
    }

    override fun generateNewMsgId(): Long {
        synchronized(this) {
            val current = IMCoreManager.getSignalModule().severTime
            if (current == lastTimestamp) {
                lastSequence++
            } else {
                lastTimestamp = current
                lastSequence = 0
            }
            return current * 100 + lastSequence
        }
    }

    override fun sendMessageToServer(message: Message): Flowable<Message> {
        return IMCoreManager.getIMApi().sendMessageToServer(message)
    }

    override fun readMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.getIMApi().readMessages(uId, sessionId, msgIds)
    }

    override fun revokeMessage(message: Message): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.getIMApi().revokeMessage(uId, message.sid, message.msgId)
    }

    override fun reeditMessage(message: Message): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.getIMApi()
            .reeditMessage(uId, message.sid, message.msgId, message.content)
    }

    override fun ackMessageToCache(sessionId: Long, msgId: Long) {
        synchronized(this) {
            if (needAckMap[sessionId] == null) {
                needAckMap[sessionId] = mutableSetOf()
            }
            needAckMap[sessionId]?.add(msgId)
        }
    }

    private fun ackMessagesSuccess(sessionId: Long, msgIds: Set<Long>) {
        synchronized(this) {
            val cacheMsgIds = needAckMap[sessionId]
            cacheMsgIds?.let {
                it.removeAll(msgIds)
                needAckMap[sessionId] = it
            }
        }
    }

    private fun ackServerMessage(sessionId: Long, msgIds: Set<Long>) {
        val uId = IMCoreManager.getUid()
        val disposable = object : BaseSubscriber<Boolean>() {
            override fun onNext(t: Boolean?) {
                t?.let {
                    if (it) {
                        ackMessagesSuccess(sessionId, msgIds)
                    }
                }
            }
        }
        IMCoreManager.getIMApi().ackMessages(uId, sessionId, msgIds)
            .compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposes.add(disposable)
    }

    override fun ackMessagesToServer() {
        synchronized(this) {
            this.needAckMap.forEach {
                this.ackServerMessage(it.key, it.value)
            }
        }
    }

    private fun deleteSeverMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.getIMApi().deleteMessages(uId, sessionId, msgIds)
    }

    private fun deleteLocalMessages(messages: List<Message>): Flowable<Boolean> {
        return Flowable.create({
            IMCoreManager.getImDataBase().messageDao().deleteMessages(messages)
            it.onNext(true)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun deleteMessages(
        sessionId: Long,
        messages: List<Message>,
        deleteServer: Boolean
    ): Flowable<Boolean> {
        if (deleteServer) {
            val msgIds = mutableSetOf<Long>()
            messages.forEach {
                msgIds.add(it.msgId)
            }
            return this.deleteSeverMessages(sessionId, msgIds).flatMap {
                if (it) {
                    return@flatMap deleteLocalMessages(messages)
                } else {
                    return@flatMap Flowable.just(false)
                }
            }
        } else {
            return deleteLocalMessages(messages)
        }
    }

    override fun processSessionByMessage(msg: Message) {
        val messageDao = IMCoreManager.getImDataBase().messageDao()
        val sessionDao = IMCoreManager.getImDataBase().sessionDao()
        val dispose = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                super.onComplete()
                if (t.mTime < msg.cTime) {
                    val processor = getMsgProcessor(msg.type)
                    t.lastMsg = processor.getSessionDesc(msg)
                    t.mTime = msg.cTime
                    t.unRead = messageDao.getUnReadCount(t.id)
                    sessionDao.updateSession(t)
                    XEventBus.post(XEventType.SessionNew.value, t)
                }

            }
        }
        getSession(msg.sid)
            .compose(RxTransform.flowableToIo())
            .subscribe(dispose)
        disposes.add(dispose)
    }

    override fun onSignalReceived(subType: Int, body: String) {
        if (subType == 0) {
            try {
                val bean = Gson().fromJson(body, MessageBean::class.java)
                val msg = bean.toMessage()
                onNewMessage(msg)
            } catch (e: Exception) {
                LLog.e("onSignalReceived err, $e")
            }
        } else {
            LLog.e("onSignalReceived err $subType, $body")
        }
    }

    private fun setOfflineMsgSyncTime(time: Long): Boolean {
        return true
    }

    private fun getOfflineMsgLastSyncTime(): Long {
        return 0
    }


//
//    override fun onSignalReceived(subType: Int, body: String) {
//        val messageBean = Gson().fromJson(body, MessageBean::class.java)
//        onNewMessage(messageBean)
//    }
//
//    override fun newMsgId(): Long {
//        synchronized(this) {
//            val current = IMCoreManager.getSignalModule().severTime
//            if (current == lastTimestamp) {
//                lastSequence++
//            } else {
//                lastTimestamp = current
//                lastSequence = 0
//            }
//            return current * 100 + lastSequence
//        }
//    }
//
//    override fun getMessageProcessor(messageType: Int): BaseMsgProcessor {
//        return IMCoreManager.getMsgProcessor(messageType)
//    }
//

//
//    override fun ackMessage(sid: Long, msgId: Long) {
//        LLog.v("ackMessage", "$sid, msgId: $msgId")
//        synchronized(this) {
//            if (needAckMap[sid] != null) {
//                needAckMap[sid]!!.add(msgId)
//            } else {
//                needAckMap[sid] = mutableListOf(msgId)
//            }
//        }
//
//        // 间隔10s发送ack
//        val now = IMCoreManager.getSignalModule().severTime
//        if (abs(now - lastAckTime) > 10 * 1000) {
//            ackMessages()
//        }
//    }
//
//    override fun ackMessages() {
//        synchronized(this) {
//            needAckMap.forEach {
//                ackMessages(it.key, it.value)
//            }
//            needAckMap.clear()
//            lastAckTime = IMCoreManager.getSignalModule().severTime
//        }
//    }
//
//    override fun ackMessages(sid: Long, msgIds: List<Long>) {
//        val body = AckMsgBean(sid, IMCoreManager.getUid(), msgIds)
//        ApiManager.getImApi(MessageApi::class.java)
//            .ackMsg(body)
//            .subscribe(object : BaseSubscriber<Void>() {
//                override fun onNext(t: Void?) {
//                    dispose()
//                }
//            })
//    }
//
//    override fun deleteServerMessages(sid: Long, msgIds: List<Long>): Flowable<Void> {
//        val uid = IMCoreManager.getUid()
//        val reqBean = DeleteMsgBean(sid, uid, msgIds)
//        return ApiManager.getImApi(MessageApi::class.java).deleteMessages(reqBean)
//    }
//
//    override fun deleteMessages(
//        sid: Long,
//        messages: List<Message>,
//        deleteServer: Boolean
//    ): Flowable<Boolean> {
//        if (!deleteServer) {
//            return Flowable.create({
//                try {
//                    for (msg in messages) {
//                        getMessageProcessor(msg.type).deleteMessage(msg)
//                    }
//                    it.onNext(true)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    it.onNext(false)
//                }
//            }, BackpressureStrategy.LATEST)
//        } else {
//            val msgIds = mutableListOf<Long>()
//            for (msg in messages) {
//                msgIds.add(msg.msgId)
//            }
//            return deleteServerMessages(sid, msgIds).flatMap {
//                try {
//                    for (msg in messages) {
//                        getMessageProcessor(msg.type).deleteMessage(msg)
//                    }
//                    Flowable.just(true)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Flowable.just(false)
//                }
//            }
//        }
//    }
//
//    override fun onNewMessage(bean: MessageBean) {
//        getMessageProcessor(bean.type).received(bean)
//    }
//
//    override fun sendMessageToServer(bean: MessageBean): Flowable<MessageBean> {
//        return ApiManager.getImApi(MessageApi::class.java).sendMsg(bean).flatMap {
//            // 服务端返回的msgId合并到新对象中
//            val finalBean = MessageBean(
//                bean.clientId, bean.fUId, bean.sessionId, it.msgId, bean.type,
//                bean.body, bean.atUsers, bean.rMsgId, it.cTime
//            )
//            return@flatMap Flowable.just(finalBean)
//        }
//    }
//
//    override fun syncLatestMessagesFromServer(
//        cTime: Long,
//        offset: Int,
//        size: Int
//    ): Flowable<List<MessageBean>> {
//        return ApiManager.getImApi(MessageApi::class.java).queryLatestMsg(
//            IMCoreManager.getUid(),
//            cTime, offset, size
//        ).flatMap {
//            Flowable.just(it.data)
//        }
//    }
//
//    override fun syncOfflineMessages(cTime: Long, offset: Int, size: Int) {
//        syncLatestMessagesFromServer(cTime, offset, size)
//            .compose(RxTransform.flowableToIo())
//            .subscribe(object : BaseSubscriber<List<MessageBean>>() {
//                override fun onNext(t: List<MessageBean>) {
//                    for (bean in t) {
//                        onNewMessage(bean)
//                    }
//                    if (t.size >= size) {
//                        syncOfflineMessages(cTime, offset + t.size, size)
//                    }
//                }
//
//                override fun onError(t: Throwable?) {
//                    super.onError(t)
//                    t?.printStackTrace()
//                }
//            })
//
//    }
//
//    override fun syncAllMessages(offset: Int, size: Int): Flowable<List<MessageBean>> {
//        val cTime = 0L
//        return ApiManager.getImApi(MessageApi::class.java).queryLatestMsg(
//            IMCoreManager.getUid(),
//            cTime, offset, size
//        ).flatMap {
//            Flowable.just(it.data)
//        }
//    }
//
//    override fun syncLatestSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>> {
//        val mTime = IMCoreManager.getSignalModule().severTime
//        return ApiManager.getImApi(SessionApi::class.java).queryLatestSession(
//            IMCoreManager.getUid(),
//            mTime, offset, size
//        )
//    }
//
//    override fun getSession(uid: Long, map: Map<String, Any>): Flowable<Session> {
//        return Flowable.create({
//            var session = IMCoreManager.getImDataBase()
//                .sessionDao().findSessionByEntity(uid, SessionType.Single.value)
//            if (session == null) {
//                session = Session(SessionType.Single.value, uid)
//            }
//            it.onNext(session)
//        }, BackpressureStrategy.LATEST).flatMap {
//            if (it.id == 0L) {
//                return@flatMap getSessionFromServerByEntityId(
//                    it.entityId,
//                    it.type,
//                    map
//                ).flatMap { bean :
//                    val session = bean.toSession()
//                    IMCoreManager.getImDataBase().sessionDao().insertSessions(session)
//                    Flowable.just(session)
//                }
//            }
//            return@flatMap Flowable.just(it)
//        }.compose(RxTransform.flowableToMain())
//    }
//
//    override fun getSessionFromServerByEntityId(
//        entityId: Long,
//        type: Int,
//        map: Map<String, Any>
//    ): Flowable<SessionBean> {
//        val selfUid = IMCoreManager.getUid()
//        val members = arrayListOf(selfUid, entityId)
//        val bean = CreateSessionBean(type, null, members)
//        return ApiManager.getImApi(SessionApi::class.java).createSession(bean)
//    }
//
//    override fun querySessionFromServer(sid: Long): Flowable<SessionBean> {
//        val selfUid = IMCoreManager.getUid()
//        return ApiManager.getImApi(SessionApi::class.java).querySession(selfUid, sid)
//    }
//
//    override fun queryLocalSession(sessionId: Long): Flowable<Session> {
//        return Flowable.create({
//            var session = IMCoreManager.getImDataBase()
//                .sessionDao().findSession(sessionId)
//            if (session == null) {
//                session = Session(sessionId)
//            }
//            it.onNext(session)
//        }, BackpressureStrategy.LATEST).flatMap {
//            if (it.entityId == 0L && it.type == 0) {
//                return@flatMap querySessionFromServer(
//                    sessionId
//                ).flatMap { bean :
//                    val session = bean.toSession()
//                    IMCoreManager.getImDataBase().sessionDao().insertSessions(session)
//                    Flowable.just(session)
//                }
//            }
//            return@flatMap Flowable.just(it)
//        }.compose(RxTransform.flowableToMain())
//    }
//
//    override fun syncAllSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>> {
//        val mTime = 0L
//        return ApiManager.getImApi(SessionApi::class.java).queryLatestSession(
//            IMCoreManager.getUid(),
//            mTime, offset, size
//        )
//    }
//
//    override fun queryLocalSessions(offset: Int, size: Int): Flowable<List<Session>> {
//        return Flowable.create(
//            {
//                val sessions =
//                    IMCoreManager.getImDataBase().sessionDao().querySessionsByMTime(offset, size)
//                it.onNext(sessions)
//            }, BackpressureStrategy.LATEST
//        ).compose(RxTransform.flowableToMain())
//    }
//
//    override fun queryLocalMessages(
//        sessionId: Long,
//        cTime: Long,
//        size: Int
//    ): Flowable<List<Message>> {
//        return Flowable.create(
//            {
//                val messages =
//                    IMCoreManager.getImDataBase().messageDao()
//                        .queryMessagesBySidAndCTime(sessionId, cTime, size)
//                it.onNext(messages)
//            }, BackpressureStrategy.LATEST
//        ).compose(RxTransform.flowableToMain())
//    }
//
//    override fun deleteServerSession(sessionList: List<Session>): Flowable<Int> {
//        return Flowable.just(0)
//    }
//
//    override fun deleteLocalSession(sessionList: List<Session>): Flowable<Int> {
//        return Flowable.create(FlowableOnSubscribe {
//            val result = IMCoreManager.getImDataBase().sessionDao()
//                .deleteSessions(*sessionList.toTypedArray())
//            it.onNext(result)
//        }, BackpressureStrategy.LATEST)
//    }
//
//    override fun deleteSession(
//        sessionList: List<Session>,
//        deleteServer: Boolean
//    ): Flowable<Boolean> {
//        return if (deleteServer) {
//            deleteServerSession(sessionList).flatMap {
//                deleteLocalSession(sessionList)
//            }.map {
//                true
//            }
//        } else {
//            deleteLocalSession(sessionList).map {
//                true
//            }
//        }
//    }
//
//    override fun signMessageReadBySessionId(sessionId: Long) {
//        IMCoreManager.getImDataBase().messageDao().updateMessagesRead(sessionId)
//    }
}