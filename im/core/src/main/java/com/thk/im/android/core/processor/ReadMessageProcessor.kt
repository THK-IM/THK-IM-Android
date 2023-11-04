package com.thk.im.android.core.processor

import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock

class ReadMessageProcessor : BaseMsgProcessor() {

    private val needReadMap = HashMap<Long, MutableSet<Long>>()
    private val readLock = ReentrantReadWriteLock()

    init {
        val subscriber = object : BaseSubscriber<Long>() {
            override fun onNext(t: Long?) {
                readMessageToServer()
            }
        }
        Flowable.interval(2, 2, TimeUnit.SECONDS)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun messageType(): Int {
        return MsgType.READ.value
    }

    override fun send(msg: Message, resend: Boolean) {
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
                    addReadMessages(t.sid, mutableSetOf(t.rMsgId!!))
                    IMCoreManager.getMessageModule().processSessionByMessage(t)
                }
            }
        }
        Flowable.create<Message>({
            try {
                IMCoreManager.getImDataBase().messageDao()
                    .clientReadMessages(msg.sid, mutableSetOf(msg.rMsgId!!))
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
        LLog.v("ReadMessageProcessor ${msg.msgId}")
        // 自己发的已读消息，更新rMsgId的消息状态为服务端已读
        if (msg.fUid == IMCoreManager.getUid()) {
            msg.rMsgId?.let {
                IMCoreManager.getImDataBase().messageDao().serverReadMessages(msg.sid, mutableSetOf(it))
            }
        } else {
            // TODO
        }
    }

    private fun addReadMessages(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor addReadMessages $msgIds")
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
    }

    private fun readMessageToServerSuccess(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor readMessageToServerSuccess $msgIds")
        try {
            readLock.writeLock().tryLock(1, TimeUnit.SECONDS)
            val cacheMsgIds = needReadMap[sessionId]
            IMCoreManager.getImDataBase().messageDao().serverReadMessages(sessionId, msgIds)
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

    private fun readServerMessage(sessionId: Long, msgIds: Set<Long>) {
        LLog.v("ReadMessageProcessor readServerMessage $msgIds")
        val uId = IMCoreManager.getUid()
        val disposable = object : BaseSubscriber<Void>() {

            override fun onComplete() {
                super.onComplete()
                readMessageToServerSuccess(sessionId, msgIds)
            }

            override fun onNext(t: Void?) {}

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.message?.let { LLog.e(it) }
            }
        }
        IMCoreManager.imApi.readMessages(uId, sessionId, msgIds)
            .compose(RxTransform.flowableToIo())
            .subscribe(disposable)
        this.disposables.add(disposable)
    }

    private fun readMessageToServer() {
        LLog.v("ReadMessageProcessor readMessageToServer")
        try {
            readLock.readLock().tryLock(1, TimeUnit.SECONDS)
            this.needReadMap.forEach {
                if (it.value.isNotEmpty()) {
                    this.readServerMessage(it.key, it.value)
                }
            }
        } catch (e: Exception) {
            LLog.e("readMessageToServer $e")
        } finally {
            readLock.readLock().unlock()
        }
    }
}