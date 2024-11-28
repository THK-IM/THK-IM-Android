package com.thk.im.android.core.module.internal

import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
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
import kotlin.math.abs


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
                m.oprStatus = m.oprStatus.or(MsgOperateStatus.Ack.value)
                    .or(MsgOperateStatus.ClientRead.value)
                    .or(MsgOperateStatus.ServerRead.value)
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

        for (sessionMessage in sessionMessages) {
            val replyMsgIds = mutableSetOf<Long>()
            for (msg in sessionMessage.value) {
                msg.rMsgId?.let {
                    replyMsgIds.add(it)
                }
            }
            if (replyMsgIds.isNotEmpty()) {
                val replyMessages =
                    IMCoreManager.db.messageDao().findByMsgIds(replyMsgIds, sessionMessage.key)
                for (msg in sessionMessage.value) {
                    msg.rMsgId?.let {
                        for (replyMessage in replyMessages) {
                            if (replyMessage.msgId == it) {
                                msg.referMsg = replyMessage
                                break
                            }
                        }
                    }
                }
            }
            XEventBus.post(
                IMEvent.BatchMsgNew.value,
                Pair(sessionMessage.key, sessionMessage.value)
            )
            // 更新每个session的最后一条消息
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
                    IMCoreManager.db.sessionDao().delete(needDelSessions)
                }
                if (needDelSIds.isNotEmpty()) {
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
                            dbSession.mergeServerSession(new)
                            IMCoreManager.db.sessionDao().update(dbSession)
                        }
                    }
                    IMCoreManager.db.sessionDao().insertOrIgnore(needUpdateSessions)
                }

                if (sessions.isNotEmpty()) {
                    val success = setSessionSyncTime(sessions.last().mTime)
                    if (success && sessions.size >= count) {
                        syncLatestSessionsFromServer()
                        return
                    }
                }
                IMCoreManager.messageModule.syncSuperGroupMessages()
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.printStackTrace()
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
                    for (m in it) {
                        if (m.cTime <= session.mTime) {
                            m.oprStatus = m.oprStatus.or(MsgOperateStatus.ClientRead.value)
                                .or(MsgOperateStatus.ServerRead.value)
                        }
                    }
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

        }
        IMCoreManager.imApi.querySessionMessages(session.id, session.msgSyncTime + 1, 0, count, 1)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        this.disposes.add(subscriber)
    }

    override fun syncSuperGroupMessages() {
        val disposable = object : BaseSubscriber<List<Session>>() {
            override fun onNext(t: List<Session>?) {
                t?.let {
                    for (s in it) {
                        if (s.deleted == 0 && s.id > 0) {
                            syncSessionMessage(s)
                        }
                    }
                }
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

    override fun syncSuperGroupMessages(session: Session) {
        if (session.deleted == 0 && session.id > 0 && session.type == SessionType.SuperGroup.value) {
            this.syncSessionMessage(session)
        }
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
        }, BackpressureStrategy.LATEST).flatMap { session ->
            if (session.id > 0) {
                return@flatMap Flowable.just(session)
            } else {
                val uId = IMCoreManager.uId
                return@flatMap IMCoreManager.imApi.queryUserSession(uId, entityId, type).flatMap {
                    if (it.id > 0 && it.deleted == 0) {
                        IMCoreManager.db.sessionDao().insertOrIgnore(listOf(it))
                        if (it.type == SessionType.SuperGroup.value) {
                            syncSessionMessage(it)
                        }
                    }
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
        }, BackpressureStrategy.LATEST).flatMap { session ->
            if (session.id > 0) {
                return@flatMap Flowable.just(session)
            } else {
                val uId = IMCoreManager.uId
                return@flatMap IMCoreManager.imApi.queryUserSession(uId, sessionId).flatMap {
                    if (it.id > 0 && it.deleted == 0) {
                        IMCoreManager.db.sessionDao().insertOrIgnore(listOf(it))
                        if (it.type == SessionType.SuperGroup.value) {
                            syncSessionMessage(it)
                        }
                    }
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
        }, BackpressureStrategy.LATEST)
    }

    override fun queryLocalMessages(
        sessionId: Long, startTime: Long, endTime: Long, count: Int
    ): Flowable<List<Message>> {
        return Flowable.create({
            val sessions = IMCoreManager.getImDataBase().messageDao()
                .findByTimeRange(sessionId, startTime, endTime, count)
            it.onNext(sessions)
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
        val current = IMCoreManager.severTime
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

    override fun deleteAllLocalSessionMessage(session: Session): Flowable<Void> {
        return Flowable.create({
            IMCoreManager.getImDataBase().messageDao().deleteBySessionId(session.id)
            XEventBus.post(IMEvent.SessionMessageClear.value, session)
            session.unReadCount = 0
            session.lastMsg = ""
            session.msgSyncTime = IMCoreManager.severTime
            IMCoreManager.getImDataBase().sessionDao().insertOrReplace(listOf(session))
            XEventBus.post(IMEvent.SessionUpdate.value, session)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun processSessionByMessage(msg: Message, forceNotify: Boolean) {
        // session为0的消息不直接显示给用户
        if (msg.sid == 0L) {
            return
        }
        val messageDao = IMCoreManager.getImDataBase().messageDao()
        val sessionDao = IMCoreManager.getImDataBase().sessionDao()
        val dispose = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                if (t.id <= 0 || t.deleted == 1) {
                    return
                }
                val unReadCount = messageDao.getUnReadCount(t.id)
                var needNotify = false
                if (t.lastMsg != null) {
                    try {
                        val lastMsg = Gson().fromJson(t.lastMsg, Message::class.java)
                        if (lastMsg.cTime <= msg.cTime) {
                            t.lastMsg = Gson().toJson(msg)
                            needNotify = true
                        }
                    } catch (e: Exception) {
                        t.lastMsg = Gson().toJson(msg)
                        needNotify = true
                        e.printStackTrace()
                    }
                } else {
                    needNotify = true
                    t.lastMsg = Gson().toJson(msg)
                }
                if (t.unReadCount != unReadCount) {
                    needNotify = true
                    t.unReadCount = unReadCount
                }
                if (needNotify || forceNotify) {
                    if (t.mTime <= msg.cTime) {
                        t.mTime = msg.cTime
                    }
                    t.unReadCount = unReadCount
                    sessionDao.insertOrReplace(listOf(t))
                    XEventBus.post(IMEvent.SessionNew.value, t)
                    if (
                        (msg.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0) &&
                        (msg.oprStatus.and(MsgOperateStatus.ServerRead.value) == 0) &&
                        !getMsgProcessor(msg.type).needReprocess(msg)
                    ) {
                        notifyNewMessage(t, msg)
                    }
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

    override fun notifyNewMessage(session: Session, message: Message) {
        if (message.type < 0 || message.fUid == IMCoreManager.uId) {
            return
        }
        if (session.status.and(SessionStatus.Silence.value) > 0) {
            return
        }
        AppUtils.instance().notifyNewMessage()
    }

    override fun querySessionMembers(
        sessionId: Long,
        needUpdate: Boolean
    ): Flowable<List<SessionMember>> {
        if (needUpdate) {
            return Flowable.just(sessionId)
                .flatMap {
                    val lastQueryTime =
                        IMCoreManager.db.sessionDao().findMemberSyncTimeById(sessionId)
                    if (abs(IMCoreManager.severTime - lastQueryTime) <= 1000 * 60) {
                        val sessionMembers =
                            IMCoreManager.db.sessionMemberDao().findBySessionId(sessionId)
                        return@flatMap Flowable.just(sessionMembers)
                    } else {
                        return@flatMap queryLastSessionMember(
                            sessionId,
                            getSessionMemberCountPerRequest()
                        )
                    }
                }
        } else {
            return Flowable.create<List<SessionMember>?>({
                val sessionMembers = IMCoreManager.db.sessionMemberDao().findBySessionId(sessionId)
                it.onNext(sessionMembers)
            }, BackpressureStrategy.LATEST).flatMap {
                if (it.isEmpty()) {
                    return@flatMap queryLastSessionMember(
                        sessionId,
                        getSessionMemberCountPerRequest()
                    )
                } else {
                    return@flatMap Flowable.just(it)
                }
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
            IMCoreManager.db.sessionMemberDao().insertOrReplace(it)
            IMCoreManager.db.sessionDao().updateMemberSyncTime(sessionId, IMCoreManager.severTime)
            if (it.size >= count) {
                return@flatMap queryLastSessionMember(sessionId, count)
            } else {
                val sessionMembers = IMCoreManager.db.sessionMemberDao().findBySessionId(sessionId)
                val memberCount =
                    IMCoreManager.db.sessionMemberDao().findSessionMemberCount(sessionId)
                val session = IMCoreManager.db.sessionDao().findById(sessionId)
                session?.let { s ->
                    if (s.memberCount != memberCount) {
                        s.memberCount = memberCount
                        IMCoreManager.db.sessionDao().update(s)
                        XEventBus.post(IMEvent.SessionUpdate.value, s)
                    }
                }
                return@flatMap Flowable.just(sessionMembers)
            }
        }
    }

    override fun syncSessionMembers(sessionId: Long) {
        val subscriber = object : BaseSubscriber<List<SessionMember>>() {
            override fun onNext(t: List<SessionMember>?) {

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
            val sessionIds = mutableSetOf<Long>()
            for (m in messages) {
                sessionIds.add(m.sid)
            }
            for (sid in sessionIds) {
                val lastSessionMsg =
                    IMCoreManager.getImDataBase().messageDao().findLastMessageBySessionId(sid)
                if (lastSessionMsg != null) {
                    processSessionByMessage(lastSessionMsg, true)
                } else {
                    val session = IMCoreManager.getImDataBase().sessionDao().findById(sid)
                    session?.let { s ->
                        s.unReadCount = 0
                        s.lastMsg = ""
                        s.msgSyncTime = IMCoreManager.severTime
                        IMCoreManager.getImDataBase().sessionDao().insertOrReplace(listOf(s))
                        XEventBus.post(IMEvent.SessionUpdate.value, s)
                    }
                }
            }
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

    override fun setAllMessageReadBySessionId(sessionId: Long): Flowable<Void> {
        return Flowable.create({
            try {
                val unReadMessages =
                    IMCoreManager.db.messageDao().findAllUnreadMessagesBySessionId(sessionId)
                for (m in unReadMessages) {
                    if (m.fUid != IMCoreManager.uId && m.msgId > 0) {
                        IMCoreManager.messageModule
                            .sendMessage(
                                m.sid, MsgType.Read.value,
                                null, null, null, m.msgId
                            )
                    }
                }
            } catch (e: Exception) {
                it.onError(e)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }


    override fun setAllMessageRead(): Flowable<Void> {
        return Flowable.create({
            try {
                val unReadMessages = IMCoreManager.db.messageDao().findAllUnreadMessages()
                for (m in unReadMessages) {
                    if (m.fUid != IMCoreManager.uId && m.msgId > 0) {
                        IMCoreManager.messageModule
                            .sendMessage(
                                m.sid, MsgType.Read.value,
                                null, null, null, m.msgId
                            )
                    }
                }
            } catch (e: Exception) {
                it.onError(e)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }
}