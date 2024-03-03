package com.thk.im.android.core.processor

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

open class IMReadMessageProcessor : IMBaseMsgProcessor() {

    private val needReadMap = HashMap<Long, MutableSet<Long>>()
    private val readLock = ReentrantReadWriteLock()
    private val readMessagePublishSubject = PublishSubject.create<Int>()

    init {
        initMessagePublishSubject()
    }

    private fun initMessagePublishSubject() {
        val consumer = Consumer<Int> { sendCacheReadMessagesToServer() }
        val subscriber = readMessagePublishSubject.debounce(this.sendInterval(), TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .subscribe(consumer)
        disposables.add(subscriber)
    }

    open fun sendInterval(): Long {
        return 2
    }

    override fun messageType(): Int {
        return MsgType.Read.value
    }

    override fun send(msg: Message, resend: Boolean, callback: IMSendMsgCallback?) {
        LLog.v("ReadMessageProcessor send ${msg.rMsgId}")
        if (msg.rMsgId == null) {
            return
        }
        if (msg.rMsgId!! < 0) {
            return
        }
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onError(t: Throwable?) {
                t?.message?.let {
                    LLog.v(it)
                }
            }

            override fun onNext(t: Message?) {
                t?.let {
                    addReadMessagesToCache(t.sid, mutableSetOf(t.rMsgId!!))
                }
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        Flowable.create<Message>({
            try {
                IMCoreManager.getImDataBase().messageDao().updateOperationStatus(
                    msg.sid, mutableSetOf(msg.rMsgId!!), MsgOperateStatus.ClientRead.value
                )
                val session = IMCoreManager.getImDataBase().sessionDao().findById(msg.sid)
                if (session != null) {
                    val count =
                        IMCoreManager.getImDataBase().messageDao().getUnReadCount(session.id)
                    if (session.unReadCount != count || session.mTime < msg.mTime) {
                        session.unReadCount = count
                        session.mTime = msg.mTime
                        IMCoreManager.getImDataBase().sessionDao().update(session)
                        XEventBus.post(IMEvent.SessionUpdate.value, session)
                    }
                }
                it.onNext(msg)
            } catch (e: Exception) {
                it.onError(e)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        this.disposables.add(subscriber)
    }

    override fun received(msg: Message) {
        val dbMsg =
            IMCoreManager.getImDataBase().messageDao().findById(msg.id, msg.fUid, msg.sid)
        if (dbMsg != null) {
            return
        }
        msg.rMsgId?.let {
            // 别人发给自己的已读消息
            val referMsg =
                IMCoreManager.getImDataBase().messageDao().findByMsgId(it, msg.sid)
            if (referMsg != null) {
                if (msg.fUid == IMCoreManager.uId) {
                    // 自己发的已读消息不插入数据库，更新rMsgId的消息状态为服务端已读
                    referMsg.oprStatus =
                        MsgOperateStatus.ServerRead.value.or(MsgOperateStatus.ClientRead.value)
                            .or(MsgOperateStatus.Ack.value)
                    referMsg.mTime = msg.cTime
                    insertOrUpdateDb(referMsg, notify = true, notifySession = false)
                    val session = IMCoreManager.getImDataBase().sessionDao().findById(msg.sid)
                    if (session != null) {
                        val count =
                            IMCoreManager.getImDataBase().messageDao().getUnReadCount(session.id)
                        if (session.unReadCount != count || session.mTime < msg.mTime) {
                            session.unReadCount = count
                            session.mTime = msg.mTime
                            IMCoreManager.getImDataBase().sessionDao().update(session)
                            XEventBus.post(IMEvent.SessionUpdate.value, session)
                        }
                    }
                } else {
                    if (referMsg.rUsers != null) {
                        referMsg.rUsers = "${referMsg.rUsers}#${msg.fUid}"
                    } else {
                        referMsg.rUsers = "${msg.fUid}"
                    }
                    referMsg.mTime = msg.cTime
                    insertOrUpdateDb(referMsg, notify = true, notifySession = false)
                    msg.sendStatus = MsgSendStatus.Success.value
                    // 状态操作消息对用户不可见，默认状态即位本身已读
                    msg.oprStatus =
                        MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
                    // 已读消息入库，并ack
                    insertOrUpdateDb(msg, notify = false, notifySession = false)
                    if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                        IMCoreManager.messageModule.ackMessageToCache(msg)
                    }
                }
            }
        }
    }

    private fun addReadMessagesToCache(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor addReadMessages $msgIds")
        val session = IMCoreManager.db.sessionDao().findById(sessionId)
        if (SessionType.SuperGroup.value == session?.type) {
            return
        }
        try {
            readLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            if (needReadMap[sessionId] == null) {
                needReadMap[sessionId] = mutableSetOf()
            }
            needReadMap[sessionId]?.addAll(msgIds)
        } catch (e: Exception) {
            LLog.e("addReadMessages $e")
        } finally {
            readLock.writeLock().unlock()
        }
        readMessagePublishSubject.onNext(0)
    }

    private fun readMessageToServerSuccess(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor readMessageToServerSuccess $msgIds")
        try {
            readLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            val cacheMsgIds = needReadMap[sessionId]
            cacheMsgIds?.let {
                it.removeAll(msgIds)
                needReadMap[sessionId] = it
            }
        } catch (e: Exception) {
            LLog.e("readMessageToServerSuccess $e")
        } finally {
            readLock.writeLock().unlock()
        }
    }

    private fun sendReadMessages(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor readServerMessage $msgIds")
        val uId = IMCoreManager.uId
        val disposable = object : BaseSubscriber<Void>() {

            override fun onComplete() {
                super.onComplete()
                IMCoreManager.getImDataBase().messageDao().updateOperationStatus(
                    sessionId, msgIds, MsgOperateStatus.ServerRead.value
                )
                readMessageToServerSuccess(sessionId, msgIds)
                disposables.remove(this)
            }

            override fun onNext(t: Void?) {}

            override fun onError(t: Throwable?) {
                t?.message?.let { LLog.e(it) }
                disposables.remove(this)
            }
        }
        IMCoreManager.imApi.readMessages(uId, sessionId, msgIds)
            .compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposables.add(disposable)
    }

    private fun sendCacheReadMessagesToServer() {
        LLog.v("ReadMessageProcessor readMessageToServer")
        try {
            readLock.readLock().tryLock(1, TimeUnit.SECONDS)
            this.needReadMap.forEach {
                if (it.value.isNotEmpty()) {
                    this.sendReadMessages(it.key, it.value)
                }
            }
        } catch (e: Exception) {
            LLog.e("readMessageToServer $e")
        } finally {
            readLock.readLock().unlock()
        }
    }

    private fun clearReadMessageCache() {
        try {
            readLock.readLock().tryLock(1, TimeUnit.SECONDS)
            this.needReadMap.clear()
        } catch (e: Exception) {
            LLog.e("clearReadMessageCache $e")
        } finally {
            readLock.readLock().unlock()
        }
    }

    override fun needReprocess(msg: Message): Boolean {
        return true
    }

    override fun reset() {
        super.reset()
        clearReadMessageCache()
        initMessagePublishSubject()
    }
}