package com.thk.im.android.core.module.internal

import com.google.gson.Gson
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.api.bean.MessageBean
import com.thk.im.android.core.event.XEventBus
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
        IMCoreManager.imApi.getLatestMessages(IMCoreManager.getUid(), lastTime, count)
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
                return@flatMap IMCoreManager.imApi
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
                return@flatMap IMCoreManager.imApi.querySession(uId, sessionId)
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
        synchronized(this) {
            getMsgProcessor(msg.type).received(msg)
        }
    }

    override fun generateNewMsgId(): Long {
        synchronized(this) {
            val current = IMCoreManager.signalModule.severTime
            if (current == lastTimestamp) {
                lastSequence++
            } else {
                lastTimestamp = current
                lastSequence = 0
            }
            return current * 100 + lastSequence
        }
    }

    override fun sendMessage(
        body: Any,
        sessionId: Long,
        type: Int,
        atUser: String?,
        replyMsgId: Long?
    ): Boolean {
        val processor = getMsgProcessor(type)
        return processor.sendMessage(body, sessionId, atUser, replyMsgId)
    }

    override fun sendMessageToServer(message: Message): Flowable<Message> {
        return IMCoreManager.imApi.sendMessageToServer(message)
    }

    override fun readMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi.readMessages(uId, sessionId, msgIds)
    }

    override fun revokeMessage(message: Message): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi.revokeMessage(uId, message.sid, message.msgId)
    }

    override fun reeditMessage(message: Message): Flowable<Boolean> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi
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
        IMCoreManager.imApi.ackMessages(uId, sessionId, msgIds)
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
        return IMCoreManager.imApi.deleteMessages(uId, sessionId, msgIds)
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
                val processor = getMsgProcessor(msg.type)
                t.lastMsg = processor.getSessionDesc(msg)
                t.mTime = msg.cTime
                t.unRead = messageDao.getUnReadCount(t.id)
                sessionDao.insertSessions(t)
                XEventBus.post(IMEvent.SessionNew.value, t)
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
}