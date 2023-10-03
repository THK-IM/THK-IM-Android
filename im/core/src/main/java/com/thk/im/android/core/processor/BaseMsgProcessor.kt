package com.thk.im.android.core.processor

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.MsgOperateStatus
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.entity.Message
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

abstract class BaseMsgProcessor {

    protected val disposables = CompositeDisposable()

    /**
     * 收到消息
     */
    @WorkerThread
    open fun received(msg: Message) {
        // 默认插入数据库
        val dbMsg = IMCoreManager.getImDataBase().messageDao().findMessage(msg.id)
        if (dbMsg == null) {
            if (msg.fUid == IMCoreManager.getUid()) {
                // 如果发件人为自己，插入前补充消息状态为已接受并已读
                msg.oprStatus =
                    msg.oprStatus or MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
            }
            msg.sendStatus = MsgSendStatus.Success.value
            insertOrUpdateDb(
                msg,
                notify = true,
                notifySession = true,
            )
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.getUid()) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
        } else {
            if (dbMsg.sendStatus != MsgSendStatus.Success.value) {
                msg.data = dbMsg.data
                msg.sendStatus = MsgSendStatus.Success.value
                if (msg.fUid == IMCoreManager.getUid()) {
                    // 如果发件人为自己，插入前补充消息状态为已接受并已读
                    msg.oprStatus =
                        msg.oprStatus or MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
                }
                insertOrUpdateDb(
                    msg,
                    notify = true,
                    notifySession = true,
                )
            }
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.getUid()) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
        }
    }

    /**
     * 创建发送消息
     */
    open fun buildSendMsg(
        body: Any, sid: Long, atUsers: String? = null, rMsgId: Long? = null
    ): Message {
        var content = ""
        var data = ""
        if (body is String) {
            content = body
        } else {
            data = Gson().toJson(body)
        }
        val id = IMCoreManager.getMessageModule().generateNewMsgId()
        val oprStatus =
            MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
        val sendStatus = MsgSendStatus.Init.value
        val type = this.messageType()
        val fUId = IMCoreManager.getUid()
        val cTime = IMCoreManager.signalModule.severTime
        // tips：msgId初始值给-id,发送成功后更新为服务端返回的msgId
        return Message(
            id,
            fUId,
            sid,
            0 - id,
            type,
            content,
            sendStatus,
            oprStatus,
            cTime,
            cTime,
            data,
            rMsgId,
            atUsers
        )
    }

    /**
     * 发送消息,逻辑流程:
     * 1、写入数据库,
     * 2、消息处理，图片压缩/视频抽帧等
     * 3、文件上传
     * 4、调用api发送消息到服务器
     */
    open fun sendMessage(
        body: Any,
        sid: Long,
        atUsers: String? = null,
        rMsgId: Long? = null,
        map: Map<String, Any> = mutableMapOf()
    ) {
        val msg = buildSendMsg(body, sid, atUsers, rMsgId)
        this.send(msg)
    }

    open fun resend(msg: Message) {
        send(msg, true)
    }

    /**
     * 重发
     */
    open fun send(msg: Message, resend: Boolean = false) {
        var originMsg = msg
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                onComplete()
                disposables.remove(this)
                insertOrUpdateDb(
                    msg,
                    notify = true,
                    notifySession = true,
                )
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                LLog.e("Message Send err $t")
                disposables.remove(this)
                originMsg.sendStatus = MsgSendStatus.SendFailed.value
                updateFailedMsgStatus(originMsg)
            }
        }
        Flowable.just(msg).flatMap {
            if (!resend) {
                insertOrUpdateDb(
                    msg,
                    notify = true,
                    notifySession = true,
                )
            }
            // 消息二次处理
            val flowable = this.reprocessingFlowable(it)
            if (flowable != null) {
                return@flatMap flowable
            } else {
                return@flatMap Flowable.just(it)
            }
        }.flatMap {
            originMsg = it
            val flowable = uploadFlowable(it)
            if (flowable != null) {
                // 消息内容上传
                it.sendStatus = MsgSendStatus.Uploading.value
                insertOrUpdateDb(
                    msg,
                    notify = true,
                    notifySession = false,
                )
                return@flatMap flowable
            } else {
                return@flatMap Flowable.just(it)
            }
        }.flatMap {
            originMsg = it
            // 消息发送到服务器
            it.sendStatus = MsgSendStatus.Sending.value
            insertOrUpdateDb(
                msg,
                notify = false,
                notifySession = false,
            )
            IMCoreManager.getMessageModule().sendMessageToServer(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
        disposables.add(subscriber)
    }


    /**
     * 【插入或更新消息状态】
     */
    open fun insertOrUpdateDb(msg: Message, notify: Boolean = true, notifySession: Boolean = true) {
        LLog.i("insertOrUpdateMessages ${msg.id} ${msg.sendStatus}, ${notify}, $notifySession")
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.insertOrUpdateMessages(mutableListOf(msg))
        if (notify) {
            XEventBus.post(IMEvent.MsgNew.value, msg)
        }
        if (notify && notifySession) {
            if (msg.sendStatus == MsgSendStatus.Sending.value || msg.sendStatus == MsgSendStatus.SendFailed.value || msg.sendStatus == MsgSendStatus.Success.value) {
                IMCoreManager.getMessageModule().processSessionByMessage(msg)
            }
        }
    }

    /**
     * 【更新消息状态】用于在调用api发送消息失败时更新本地数据库消息状态
     */
    open fun updateFailedMsgStatus(msg: Message, notify: Boolean = true) {
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.updateSendStatus(msg.sid, msg.id, MsgSendStatus.SendFailed.value, msg.fUid)
        if (notify) {
            XEventBus.post(IMEvent.MsgNew.value, msg)
            if (msg.sendStatus == MsgSendStatus.Sending.value || msg.sendStatus == MsgSendStatus.SendFailed.value || msg.sendStatus == MsgSendStatus.Success.value) {
                IMCoreManager.getMessageModule().processSessionByMessage(msg)
            }
        }
    }


    open fun getSessionDesc(msg: Message): String {
        return if (msg.content == null) {
            ""
        } else {
            msg.content!!
        }
    }

    /**
     * 图片压缩/视频抽帧等消息内容操作二次处理
     */
    open fun reprocessingFlowable(message: Message): Flowable<Message>? {
        return null
    }

    /**
     * 消息内容上传
     */
    open fun uploadFlowable(entity: Message): Flowable<Message>? {
        return null
    }

    /**
     * 消息内容下载
     */
    open fun downloadMsgContent(entity: Message, resourceType: String) {}


    /**
     * 消息是否在界面上显示，撤回/已读/已接受等状态消息不显示
     */
    open fun isShow(msg: Message): Boolean {
        return true
    }

    /**
     * 消息是否可以删除
     */
    open fun canDeleted(msg: Message): Boolean {
        return isShow(msg)
    }

    /**
     * 消息是否可以撤回
     */
    open fun canRevoke(msg: Message): Boolean {
        return canDeleted(msg)
    }

    /**
     * 消息类型, ex: 文本(1)
     */
    abstract fun messageType(): Int

}