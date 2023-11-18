package com.thk.im.android.core.module.internal

import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.api.bean.MessageBean
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.core.db.MsgOperateStatus
import com.thk.im.android.core.db.MsgSendStatus
import com.thk.im.android.core.db.SessionType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 内部默认的消息处理器
 */
open class DefaultMessageModule : MessageModule {

    private val startSequence = 0
    private val spName = "THK_IM"
    private val lastSyncMsgTime = "Last_Sync_Message_Time"
    private val processorMap: MutableMap<Int, BaseMsgProcessor> = HashMap()
    private var lastTimestamp: Long = 0
    private var lastSequence: Int = 0
    private val epoch = 1288834974657L
    private val needAckMap = HashMap<Long, MutableSet<Long>>()
    private val disposes = CompositeDisposable()
    private val idLock = ReentrantLock()
    private val ackLock = ReentrantReadWriteLock()
    private val snowFlakeMachine: Long = 2 // 雪花算法机器编号 IOS:1 Android: 2

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
        LLog.v("syncOfflineMessages $lastTime")
        val disposable = object : BaseSubscriber<List<Message>>() {
            override fun onNext(messages: List<Message>) {
                try {
                    val sessionMessages = mutableMapOf<Long, MutableList<Message>>()
                    val unProcessorMessages = mutableListOf<Message>()
                    for (m in messages) {
                        if (m.fUid == IMCoreManager.getUid()) {
                            m.oprStatus =
                                m.oprStatus or MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
                        }
                        m.sendStatus = MsgSendStatus.Success.value
                        if (m.type < 0) {
                            // 状态操作消息交给对应消息处理器自己处理
                            getMsgProcessor(m.type).received(m)
                        } else {
                            // 其他消息批量处理
                            if (sessionMessages[m.sid] == null) {
                                sessionMessages[m.sid] = mutableListOf(m)
                            } else {
                                sessionMessages[m.sid]!!.add(m)
                            }
                            unProcessorMessages.add(m)
                        }
                    }
                    // 消息入库并ACK
                    if (unProcessorMessages.isNotEmpty()) {
                        // 插入数据库
                        IMCoreManager.getImDataBase().messageDao().insertOrIgnoreMessages(messages)
                        for (m in unProcessorMessages) {
                            if (m.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                                ackMessageToCache(m)
                            }
                        }
                    }

                    // 更新每个session的最后一条消息
                    for (sessionMessage in sessionMessages) {
                        XEventBus.post(IMEvent.BatchMsgNew.value, sessionMessage.value)
                        val lastMsg = IMCoreManager.getImDataBase().messageDao()
                            .findLastMessageBySessionId(sessionMessage.key)
                        lastMsg?.let {
                            processSessionByMessage(it)
                        }
                    }

                } catch (e: Exception) {
                    e.message?.let { LLog.e(it) }
                }

                if (messages.isNotEmpty()) {
                    val severTime = messages.last().cTime
                    val success = setOfflineMsgSyncTime(severTime)
                    if (success) {
                        if (messages.count() >= count) {
                            syncOfflineMessages()
                        }
                    }
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.printStackTrace()
            }
        }
        IMCoreManager.imApi.getLatestMessages(IMCoreManager.getUid(), lastTime, count)
            .compose(RxTransform.flowableToIo()).subscribe(disposable)
        this.disposes.add(disposable)
    }

    override fun syncLatestSessionsFromServer(lastSyncTime: Int, count: Int) {

    }

    override fun createSingleSession(entityId: Long): Flowable<Session> {
        LLog.v("createSingleSession $entityId")
        val sessionType = SessionType.Single.value
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
                val uId = IMCoreManager.getUid()
                return@flatMap IMCoreManager.imApi.createSession(uId, sessionType, entityId, null)
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
        sessionId: Long, cTime: Long, count: Int
    ): Flowable<List<Message>> {
        return Flowable.create({
            val sessions = IMCoreManager.getImDataBase().messageDao()
                .queryMessagesBySidAndCTime(sessionId, cTime, count)
            it.onNext(sessions)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun deleteSession(
        session: Session, deleteServer: Boolean
    ): Flowable<Void> {
        return if (deleteServer) {
            deleteSeverSession(session).concatWith(deleteLocalSession(session))
        } else {
            deleteLocalSession(session)
        }
    }

    override fun updateSession(session: Session, updateServer: Boolean): Flowable<Void> {
        return if (updateServer) {
            updateSeverSession(session).concatWith(updateLocalSession(session))
        } else {
            updateLocalSession(session)
        }
    }

    override fun onNewMessage(msg: Message) {
        getMsgProcessor(msg.type).received(msg)
    }

    override fun generateNewMsgId(): Long {
        val current = IMCoreManager.getCommonModule().getSeverTime()
        try {
            idLock.tryLock(1, TimeUnit.SECONDS)
            if (current == lastTimestamp) {
                lastSequence++
            } else {
                lastTimestamp = current
                lastSequence = startSequence
            }
            val seq = (current-epoch).shl(22).or(snowFlakeMachine.shl(12)).or(lastSequence.toLong())
            idLock.unlock()
            return seq
        } catch (e: Exception) {
            LLog.e("generateNewMsgId err: $e")
        }
        return (current-epoch).shl(22).or(snowFlakeMachine.shl(12)).or(1000L)
    }

    override fun sendMessage(
        body: Any, sessionId: Long, type: Int, atUser: String?, replyMsgId: Long?
    ) {
        val processor = getMsgProcessor(type)
        processor.sendMessage(body, sessionId, atUser, replyMsgId)
    }

    override fun resend(msg: Message) {
        val processor = getMsgProcessor(msg.type)
        processor.resend(msg)
    }

    override fun sendMessageToServer(message: Message): Flowable<Message> {
        return IMCoreManager.imApi.sendMessageToServer(message)
    }

    override fun revokeMessage(message: Message): Flowable<Void> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi.revokeMessage(uId, message.sid, message.msgId)
    }

    override fun reeditMessage(message: Message): Flowable<Void> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi.reeditMessage(uId, message.sid, message.msgId, message.content)
    }

    override fun ackMessageToCache(message: Message) {
        try {
            ackLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            if (message.sid > 0 && message.msgId > 0) {
                if (message.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                    if (needAckMap[message.sid] == null) {
                        needAckMap[message.sid] = mutableSetOf()
                    }
                    needAckMap[message.sid]?.add(message.msgId)
                }
            }
        } catch (e: Exception) {
            LLog.e("ackMessageToCache $e")
        } finally {
            ackLock.writeLock().unlock()
        }
    }

    private fun ackMessagesSuccess(sessionId: Long, msgIds: Set<Long>) {
        try {
            ackLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            val cacheMsgIds = needAckMap[sessionId]
            cacheMsgIds?.let {
                it.removeAll(msgIds)
                needAckMap[sessionId] = it
            }
            ackLock.writeLock().unlock()
        } catch (e: Exception) {
            LLog.e("ackMessageToCache $e")
        }
    }

    override fun ackMessagesToServer() {
        try {
            ackLock.readLock().tryLock(1, TimeUnit.SECONDS)
            this.needAckMap.forEach {
                if (it.value.isNotEmpty()) {
                    this.ackServerMessage(it.key, it.value)
                }
            }
        } catch (e: Exception) {
            LLog.e("ackMessageToCache $e")
        } finally {
            ackLock.readLock().unlock()
        }
    }

    override fun deleteMessages(
        sessionId: Long, messages: List<Message>, deleteServer: Boolean
    ): Flowable<Void> {
        return if (deleteServer) {
            val msgIds = mutableSetOf<Long>()
            messages.forEach {
                if (it.msgId > 0) {
                    msgIds.add(it.msgId)
                }
            }
            this.deleteSeverMessages(sessionId, msgIds).concatWith(deleteLocalMessages(messages))
        } else {
            deleteLocalMessages(messages)
        }
    }

    override fun processSessionByMessage(msg: Message) {
        val messageDao = IMCoreManager.getImDataBase().messageDao()
        val sessionDao = IMCoreManager.getImDataBase().sessionDao()
        val dispose = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                val unReadCount = messageDao.getUnReadCount(t.id)
                if (t.mTime < msg.mTime || t.unRead != unReadCount) {
                    val processor = getMsgProcessor(msg.type)
                    t.lastMsg = processor.getSessionDesc(msg)
                    t.mTime = msg.mTime
                    t.unRead = unReadCount
                    sessionDao.insertOrUpdateSessions(t)
                    XEventBus.post(IMEvent.SessionNew.value, t)
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                LLog.e("processSessionByMessage error $t")
            }
        }
        getSession(msg.sid).compose(RxTransform.flowableToIo()).subscribe(dispose)
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
        val app = IMCoreManager.getApplication()
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong(lastSyncMsgTime, time)
        return editor.commit()
    }

    private fun getOfflineMsgLastSyncTime(): Long {
        val app = IMCoreManager.getApplication()
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        return sp.getLong(lastSyncMsgTime, 0)
    }

    private fun updateSeverSession(session: Session): Flowable<Void> {
        return IMCoreManager.imApi.updateSession(IMCoreManager.getUid(), session)
    }

    private fun updateLocalSession(session: Session): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().sessionDao().updateSession(session)
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.SessionUpdate.value, session)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun deleteSeverSession(session: Session): Flowable<Void> {
        return IMCoreManager.imApi.deleteSession(IMCoreManager.getUid(), session)
    }

    private fun deleteLocalSession(session: Session): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().sessionDao().deleteSessions(session)
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.SessionDelete.value, session)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun deleteSeverMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Void> {
        val uId = IMCoreManager.getUid()
        return IMCoreManager.imApi.deleteMessages(uId, sessionId, msgIds)
    }

    private fun deleteLocalMessages(messages: List<Message>): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().messageDao().deleteMessages(messages)
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.BatchMsgDelete.value, messages)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun ackServerMessage(sessionId: Long, msgIds: Set<Long>) {
        val uId = IMCoreManager.getUid()
        val disposable = object : BaseSubscriber<Void>() {

            override fun onComplete() {
                super.onComplete()
                IMCoreManager.getImDataBase().messageDao().updateMessageOperationStatus(
                    sessionId, msgIds, MsgOperateStatus.Ack.value
                )
                ackMessagesSuccess(sessionId, msgIds)
            }

            override fun onNext(t: Void?) {}

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.message?.let { LLog.e(it) }
            }
        }
        IMCoreManager.imApi.ackMessages(uId, sessionId, msgIds).compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposes.add(disposable)
    }
}