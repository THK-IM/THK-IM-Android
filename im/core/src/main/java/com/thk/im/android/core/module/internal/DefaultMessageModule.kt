package com.thk.im.android.core.module.internal

import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.SessionStatus
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.api.vo.MessageVo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
    private val lastSyncSessionTime = "Last_Sync_Session_Time"
    private val processorMap: MutableMap<Int, IMBaseMsgProcessor> = HashMap()
    private var lastTimestamp: Long = 0
    private var lastSequence: Int = 0
    private val epoch = 1288834974657L
    private val disposes = CompositeDisposable()
    private val idLock = ReentrantLock()
    private val ackLock = ReentrantReadWriteLock()
    private val snowFlakeMachine: Long = 2 // 雪花算法机器编号 Ios:1 Android:2
    private val needAckMap = HashMap<Long, MutableSet<Long>>()
    private val ackMessagePublishSubject = PublishSubject.create<Int>()

    init {
        initAckMessagePublishSubject()
    }

    private fun initAckMessagePublishSubject() {
        val consumer = Consumer<Int> { ackMessagesToServer() }
        val subscriber = ackMessagePublishSubject.debounce(this.ackInterval(), TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .subscribe(consumer)
        disposes.add(subscriber)
    }

    open fun ackInterval(): Long {
        return 5
    }

    open fun getOfflineMsgCountPerRequest(): Int {
        return 200
    }

    open fun getSessionCountPerRequest(): Int {
        return 200
    }

    open fun getSessionMemberCountPerRequest(): Int {
        return 100
    }

    override fun registerMsgProcessor(processor: IMBaseMsgProcessor) {
        processorMap[processor.messageType()] = processor
    }

    override fun getMsgProcessor(msgType: Int): IMBaseMsgProcessor {
        val processor = processorMap[msgType]
        return processor ?: processorMap[0]!!
    }

    @Throws(Exception::class)
    private fun batchProcessMessages(messages: List<Message>, ack: Boolean = true) {
        val sessionMessages = mutableMapOf<Long, MutableList<Message>>()
        val unProcessorMessages = mutableListOf<Message>()
        val needProcessMessages = mutableListOf<Message>()
        for (m in messages) {
            if (m.fUid == IMCoreManager.uId) {
                m.oprStatus =
                    m.oprStatus or MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
            }
            m.sendStatus = MsgSendStatus.Success.value
            if (getMsgProcessor(m.type).needReprocess(m)) {
                // 状态操作消息交给对应消息处理器自己处理
                needProcessMessages.add(m)
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
            IMCoreManager.getImDataBase().messageDao()
                .insertOrIgnore(unProcessorMessages)
            if (ack) {
                for (m in unProcessorMessages) {
                    if (m.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                        ackMessageToCache(m)
                    }
                }
            }
        }

        for (m in needProcessMessages) {
            getMsgProcessor(m.type).received(m)
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
    }

    override fun syncOfflineMessages() {
        val lastTime = this.getOfflineMsgLastSyncTime()
        val count = this.getOfflineMsgCountPerRequest()
        LLog.v("syncOfflineMessages $lastTime")
        val disposable = object : BaseSubscriber<List<Message>>() {
            override fun onNext(messages: List<Message>) {
                try {
                    batchProcessMessages(messages)
                    if (messages.isNotEmpty()) {
                        val severTime = messages.last().cTime
                        val success = setOfflineMsgSyncTime(severTime)
                        if (success && messages.count() >= count) {
                            syncOfflineMessages()
                        }
                    }
                } catch (e: Exception) {
                    e.message?.let { LLog.e(it) }
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.printStackTrace()
            }

            override fun onComplete() {
                super.onComplete()
                this@DefaultMessageModule.disposes.remove(this)
            }
        }
        IMCoreManager.imApi.queryUserLatestMessages(IMCoreManager.uId, lastTime, count)
            .compose(RxTransform.flowableToIo()).subscribe(disposable)
        this.disposes.add(disposable)
    }

    override fun syncLatestSessionsFromServer() {
        val lastTime = this.getSessionLastSyncTime()
        val count = this.getSessionCountPerRequest()
        LLog.v("syncLatestSessionsFromServer $lastTime")
        val disposable = object : BaseSubscriber<List<Session>>() {
            override fun onNext(sessions: List<Session>) {
                val needDelGroups = mutableListOf<Long>()
                val needDelSIds = mutableSetOf<Long>()
                val needDelSessions = mutableListOf<Session>()
                val needUpdateSessions = mutableListOf<Session>()
                for (s in sessions) {
                    if (s.deleted == 1) {
                        needDelSIds.add(s.id)
                        needDelSessions.add(s)
                        if (s.type == SessionType.Group.value ||
                            s.type == SessionType.SuperGroup.value
                        ) {
                            needDelGroups.add(s.entityId)
                        }
                    } else {
                        needUpdateSessions.add(s)
                    }
                }
                // 删除掉该删除的session
                if (needDelSessions.isNotEmpty()) {
                    IMCoreManager.db.sessionDao().delete(needUpdateSessions)
                    IMCoreManager.db.messageDao().deleteBySessionIds(needDelSIds)
                }
                if (needDelGroups.isNotEmpty()) {
                    IMCoreManager.db.groupDao().deleteByIds(needDelGroups.toSet())
                }
                if (needUpdateSessions.isNotEmpty()) {
                    for (new in needUpdateSessions) {
                        val dbSession = IMCoreManager.db.sessionDao().findById(new.id)
                        // 更新session中的在线数据信息
                        if (dbSession != null) {
                            dbSession.parentId = new.parentId
                            dbSession.entityId = new.entityId
                            dbSession.name = new.name
                            dbSession.noteName = new.noteName
                            dbSession.type = new.type
                            dbSession.remark = new.remark
                            dbSession.role = new.role
                            dbSession.status = new.status
                            dbSession.mute = new.mute
                            dbSession.extData = new.extData
                            dbSession.topTimestamp = new.topTimestamp
                            IMCoreManager.db.sessionDao().update(dbSession)
                        }
                    }
                    IMCoreManager.db.sessionDao().insertOrIgnore(needUpdateSessions)
                }

                if (sessions.isNotEmpty()) {
                    val success = setSessionSyncTime(sessions.last().mTime)
                    if (success && sessions.size >= count) {
                        syncLatestSessionsFromServer()
                    }
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.printStackTrace()
            }

            override fun onComplete() {
                super.onComplete()
                this@DefaultMessageModule.disposes.remove(this)
            }
        }
        IMCoreManager.imApi.queryUserLatestSessions(IMCoreManager.uId, count, lastTime, null)
            .compose(RxTransform.flowableToIo()).subscribe(disposable)
        this.disposes.add(disposable)
    }

    private fun syncSessionMessage(session: Session) {
        val count = getOfflineMsgCountPerRequest()
        val subscriber = object : BaseSubscriber<List<Message>>() {
            override fun onNext(t: List<Message>?) {
                t?.let {
                    batchProcessMessages(it, false)
                    if (it.isNotEmpty()) {
                        val lastTime = it.last().cTime
                        IMCoreManager.db.sessionDao().updateMsgSyncTime(session.id, lastTime)
                        if (it.count() >= count) {
                            session.msgSyncTime = lastTime
                            syncSessionMessage(session)
                        }
                    }
                }
            }

            override fun onComplete() {
                super.onComplete()
                this@DefaultMessageModule.disposes.remove(this)
            }

        }
        IMCoreManager.imApi.querySessionMessages(session.id, session.msgSyncTime, 0, count, 1)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        this.disposes.add(subscriber)
    }

    override fun syncSuperGroupMessages() {
        val disposable = object : BaseSubscriber<List<Session>>() {
            override fun onNext(t: List<Session>?) {
                t?.let {
                    for (s in it) {
                        syncSessionMessage(s)
                    }
                }
            }

            override fun onComplete() {
                super.onComplete()
                this@DefaultMessageModule.disposes.remove(this)
            }
        }
        Flowable.just("")
            .flatMap {
                val superGroupSessions =
                    IMCoreManager.db.sessionDao().findAll(SessionType.SuperGroup.value)
                Flowable.just(superGroupSessions)
            }.compose(RxTransform.flowableToMain())
            .subscribe(disposable)
        this.disposes.add(disposable)
    }

    override fun getSession(entityId: Long, type: Int): Flowable<Session> {
        return Flowable.create<Session>({
            val session =
                IMCoreManager.getImDataBase().sessionDao().findByEntityId(entityId, type)
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
                val uId = IMCoreManager.uId
                return@flatMap IMCoreManager.imApi.queryUserSession(uId, entityId, type).flatMap {
                    IMCoreManager.db.sessionDao().insertOrIgnore(listOf(it))
                    Flowable.just(it)
                }
            }
        }
    }


    override fun getSession(sessionId: Long): Flowable<Session> {
        return Flowable.create<Session>({
            val session = IMCoreManager.getImDataBase().sessionDao().findById(sessionId)
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
                val uId = IMCoreManager.uId
                return@flatMap IMCoreManager.imApi.queryUserSession(uId, sessionId).flatMap {
                    IMCoreManager.db.sessionDao().insertOrIgnore(listOf(it))
                    Flowable.just(it)
                }
            }
        }
    }

    override fun queryLocalSessions(
        parentId: Long,
        count: Int,
        mTime: Long
    ): Flowable<List<Session>> {
        return Flowable.create({
            val sessions =
                IMCoreManager.getImDataBase().sessionDao().findByParentId(parentId, count, mTime)
            it.onNext(sessions)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun queryLocalMessages(
        sessionId: Long, startTime: Long, endTime: Long, count: Int
    ): Flowable<List<Message>> {
        return Flowable.create({
            val sessions = IMCoreManager.getImDataBase().messageDao()
                .findByTimeRange(sessionId, startTime, endTime, count)
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
        val current = IMCoreManager.commonModule.getSeverTime()
        try {
            idLock.tryLock(1, TimeUnit.SECONDS)
            if (current == lastTimestamp) {
                lastSequence++
            } else {
                lastTimestamp = current
                lastSequence = startSequence
            }
            val seq =
                (current - epoch).shl(22).or(snowFlakeMachine.shl(12)).or(lastSequence.toLong())
            idLock.unlock()
            return seq
        } catch (e: Exception) {
            LLog.e("generateNewMsgId err: $e")
        }
        return (current - epoch).shl(22).or(snowFlakeMachine.shl(12)).or(1000L)
    }

    override fun sendMessage(
        sessionId: Long,
        type: Int,
        body: Any?,
        data: Any?,
        atUser: String?,
        replyMsgId: Long?,
        callback: IMSendMsgCallback?
    ) {
        val processor = getMsgProcessor(type)
        processor.sendMessage(sessionId, body, data, atUser, replyMsgId, callback)
    }

    override fun resend(msg: Message, callback: IMSendMsgCallback?) {
        val processor = getMsgProcessor(msg.type)
        processor.resend(msg, callback)
    }

    override fun sendMessageToServer(message: Message): Flowable<Message> {
        return IMCoreManager.imApi.sendMessageToServer(message)
    }

    override fun ackMessageToCache(message: Message) {
        try {
            ackLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            if (message.msgId > 0) {
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
        ackMessagePublishSubject.onNext(0)
    }

    private fun clearAckCache() {
        try {
            ackLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            needAckMap.clear()
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
        // session为0的消息不直接显示给用户
        if (msg.sid == 0L) {
            return
        }
        val messageDao = IMCoreManager.getImDataBase().messageDao()
        val sessionDao = IMCoreManager.getImDataBase().sessionDao()
        val dispose = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                val unReadCount = messageDao.getUnReadCount(t.id)
                if (t.mTime <= msg.mTime || t.unReadCount != unReadCount) {
                    val processor = getMsgProcessor(msg.type)
                    var statusText = ""
                    if (msg.sendStatus == MsgSendStatus.Sending.value ||
                        msg.sendStatus == MsgSendStatus.Init.value ||
                        msg.sendStatus == MsgSendStatus.Uploading.value
                    ) {
                        statusText = "⬅️"
                    } else if (msg.sendStatus == MsgSendStatus.SendFailed.value) {
                        statusText = "❗"
                    }
                    t.lastMsg = "$statusText${processor.getSessionDesc(msg)}"
                    t.mTime = msg.mTime
                    t.unReadCount = unReadCount
                    sessionDao.insertOrReplace(listOf(t))
                    XEventBus.post(IMEvent.SessionNew.value, t)
                    notifyNewMessage(t, msg)
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                LLog.e("processSessionByMessage error $t")
            }

            override fun onComplete() {
                super.onComplete()
                disposes.remove(this)
            }
        }
        getSession(msg.sid).compose(RxTransform.flowableToIo()).subscribe(dispose)
        disposes.add(dispose)
    }

    override fun notifyNewMessage(session: Session, message: Message) {
        if (message.type < 0 || message.fUid == IMCoreManager.uId) {
            return
        }
        if (session.status.and(SessionStatus.Silence.value) > 0) {
            return
        }
        AppUtils.instance().notifyNewMessage()
    }

    override fun querySessionMembers(sessionId: Long): Flowable<List<SessionMember>> {
        return Flowable.create<List<SessionMember>?>({
            val sessionMembers = IMCoreManager.db.sessionMemberDao().findBySessionId(sessionId)
            it.onNext(sessionMembers)
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.isEmpty()) {
                return@flatMap queryLastSessionMember(sessionId, getSessionMemberCountPerRequest())
            } else {
                return@flatMap Flowable.just(it)
            }
        }
    }

    private fun queryLastSessionMember(sessionId: Long, count: Int): Flowable<List<SessionMember>> {
        return Flowable.just(sessionId).flatMap {
            val mTime = IMCoreManager.db.sessionDao().findMemberSyncTimeById(sessionId)
            return@flatMap Flowable.just(mTime)
        }.flatMap {
            return@flatMap IMCoreManager.imApi.queryLatestSessionMembers(sessionId, it, null, count)
        }.flatMap {
            val inserts = mutableListOf<SessionMember>()
            val deletes = mutableListOf<SessionMember>()
            for (sm in it) {
                if (sm.deleted == 0) {
                    inserts.add(sm)
                } else {
                    deletes.add(sm)
                }
            }
            IMCoreManager.db.sessionMemberDao().insertOrReplace(inserts)
            IMCoreManager.db.sessionMemberDao().delete(deletes)
            if (it.isNotEmpty()) {
                val mTime = it.last().mTime
                IMCoreManager.db.sessionDao().updateMemberSyncTime(sessionId, mTime)
            }
            if (it.size >= count) {
                return@flatMap queryLastSessionMember(sessionId, count)
            } else {
                val sessionMembers = IMCoreManager.db.sessionMemberDao().findBySessionId(sessionId)
                return@flatMap Flowable.just(sessionMembers)
            }
        }
    }

    override fun syncSessionMembers(sessionId: Long) {
        val subscriber = object : BaseSubscriber<List<SessionMember>>() {
            override fun onNext(t: List<SessionMember>?) {

            }

            override fun onComplete() {
                super.onComplete()
                disposes.remove(this)
            }
        }
        val count = getSessionMemberCountPerRequest()
        queryLastSessionMember(sessionId, count).compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        disposes.add(subscriber)

    }

    override fun reset() {
        clearAckCache()
        disposes.clear()
        for ((_, v) in processorMap) {
            v.reset()
        }
        initAckMessagePublishSubject()
    }

    override fun onSignalReceived(type: Int, body: String) {
        try {
            val bean = Gson().fromJson(body, MessageVo::class.java)
            val msg = bean.toMessage()
            onNewMessage(msg)
        } catch (e: Exception) {
            LLog.e("onSignalReceived err, $e, $type, $body")
        }
    }

    private fun setSessionSyncTime(time: Long): Boolean {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong("${lastSyncSessionTime}_${IMCoreManager.uId}", time)
        return editor.commit()
    }

    private fun getSessionLastSyncTime(): Long {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        return sp.getLong("${lastSyncSessionTime}_${IMCoreManager.uId}", 0)
    }

    private fun setOfflineMsgSyncTime(time: Long): Boolean {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong("${lastSyncMsgTime}_${IMCoreManager.uId}", time)
        return editor.commit()
    }

    private fun getOfflineMsgLastSyncTime(): Long {
        val app = IMCoreManager.app
        val sp = app.getSharedPreferences(spName, MODE_PRIVATE)
        return sp.getLong("${lastSyncMsgTime}_${IMCoreManager.uId}", 0)
    }

    private fun updateSeverSession(session: Session): Flowable<Void> {
        return IMCoreManager.imApi.updateUserSession(IMCoreManager.uId, session)
    }

    private fun updateLocalSession(session: Session): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().sessionDao().update(session)
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.SessionUpdate.value, session)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun deleteSeverSession(session: Session): Flowable<Void> {
        return IMCoreManager.imApi.deleteUserSession(IMCoreManager.uId, session)
    }

    private fun deleteLocalSession(session: Session): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().messageDao().deleteBySessionId(session.id)
                IMCoreManager.getImDataBase().sessionDao().delete(listOf(session))
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.SessionDelete.value, session)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun deleteSeverMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Void> {
        val uId = IMCoreManager.uId
        return IMCoreManager.imApi.deleteMessages(uId, sessionId, msgIds)
    }

    private fun deleteLocalMessages(messages: List<Message>): Flowable<Void> {
        return Flowable.create({
            try {
                IMCoreManager.getImDataBase().messageDao().delete(messages)
            } catch (e: Exception) {
                it.onError(e)
            }
            XEventBus.post(IMEvent.BatchMsgDelete.value, messages)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    private fun ackServerMessage(sessionId: Long, msgIds: Set<Long>) {
        val uId = IMCoreManager.uId
        val disposable = object : BaseSubscriber<Void>() {

            override fun onComplete() {
                super.onComplete()
                IMCoreManager.getImDataBase().messageDao().updateOperationStatus(
                    sessionId, msgIds, MsgOperateStatus.Ack.value
                )
                ackMessagesSuccess(sessionId, msgIds)
                disposes.remove(this)
            }

            override fun onNext(t: Void?) {}

            override fun onError(t: Throwable?) {
                t?.message?.let { LLog.e(it) }
                disposes.remove(this)
            }
        }
        IMCoreManager.imApi.ackMessages(uId, sessionId, msgIds)
            .compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposes.add(disposable)
    }
}