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

abstract class BaseMsgProcessor {

    /**
     * 创建发送消息
     */
    open fun buildSendMsg(
        body: Any, sid: Long,
        atUsers: String? = null, rMsgId: Long? = null
    ): Message {
        var content = ""
        var data = ""
        if (body is String) {
            content = body
        } else {
            data = Gson().toJson(body)
        }
        val id = IMCoreManager.getMessageModule().generateNewMsgId()
        val oprStatus = MsgOperateStatus.Ack.value or
                MsgOperateStatus.ClientRead.value or
                MsgOperateStatus.ServerRead.value
        val sendStatus = MsgSendStatus.Init.value
        val type = this.messageType()
        val fUId = IMCoreManager.getUid()
        val cTime = IMCoreManager.signalModule.severTime
        // tips：msgId初始值给-id,发送成功后更新为服务端返回的msgId
        return Message(
            id, fUId, sid, 0 - id, type, content, oprStatus, sendStatus,
            cTime, cTime, data, rMsgId, atUsers
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
        body: Any, sid: Long,
        atUsers: String? = null, rMsgId: Long? = null, map: Map<String, Any> = mutableMapOf()
    ): Boolean {
        try {
            val msg = buildSendMsg(body, sid, atUsers, rMsgId)
            this.resend(msg)
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return false
        }
        return true
    }

    /**
     * 重发
     */
    open fun resend(msg: Message) {
        var originMsg = msg
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                super.onComplete()
                insertOrUpdateDb(t)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                originMsg.sendStatus = MsgSendStatus.SendFailed.value
                updateFailedMsgStatus(originMsg)
            }
        }
        Flowable.just(msg).flatMap {
            // 消息二次处理
            val flowable = this.reprocessingFlowable(it)
            if (flowable != null) {
                return@flatMap flowable
            } else {
                return@flatMap Flowable.just(it)
            }
        }.flatMap {
            originMsg = it
            // 消息内容上传
            it.sendStatus = MsgSendStatus.Uploading.value
            insertOrUpdateDb(it)
            val flowable = uploadFlowable(it)
            if (flowable != null) {
                return@flatMap flowable
            } else {
                return@flatMap Flowable.just(it)
            }
        }.flatMap {
            originMsg = it
            // 消息发送到服务器
            it.sendStatus = MsgSendStatus.Sending.value
            insertOrUpdateDb(it)
            IMCoreManager.getMessageModule().sendMessageToServer(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }


    /**
     * 【插入或更新消息状态】
     */
    open fun insertOrUpdateDb(msg: Message, notify: Boolean = true) {
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.insertOrUpdateMessages(mutableListOf(msg))
        if (notify) {
            XEventBus.post(IMEvent.MsgNew.value, msg)
        }
        if (msg.sendStatus == MsgSendStatus.Sending.value
            || msg.sendStatus == MsgSendStatus.SendFailed.value
            || msg.sendStatus == MsgSendStatus.Success.value
        ) {
            IMCoreManager.getMessageModule().processSessionByMessage(msg)
        }
    }

    /**
     * 【更新消息状态】用于在调用api发送消息失败时更新本地数据库消息状态
     */
    open fun updateFailedMsgStatus(msg: Message) {
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.updateSendStatus(msg.sid, msg.id, MsgSendStatus.SendFailed.value, msg.fUid)
        if (msg.sendStatus == MsgSendStatus.Sending.value
            || msg.sendStatus == MsgSendStatus.SendFailed.value
            || msg.sendStatus == MsgSendStatus.Success.value
        ) {
            IMCoreManager.getMessageModule().processSessionByMessage(msg)
        }
    }


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
                msg.oprStatus = msg.oprStatus or
                        MsgOperateStatus.Ack.value or
                        MsgOperateStatus.ClientRead.value or
                        MsgOperateStatus.ServerRead.value
                msg.sendStatus = MsgSendStatus.Success.value
            }
            insertOrUpdateDb(msg)
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
            IMCoreManager.getMessageModule().processSessionByMessage(msg)
        } else {
            if (dbMsg.sendStatus != MsgSendStatus.Success.value) {
                msg.data = dbMsg.data
                msg.oprStatus = dbMsg.oprStatus
                msg.msgId = dbMsg.msgId
                msg.sendStatus = MsgSendStatus.Success.value
                insertOrUpdateDb(msg)
            }
            if (dbMsg.oprStatus.and(MsgOperateStatus.Ack.value) == 0) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
            if (dbMsg.oprStatus.and(MsgOperateStatus.ClientRead.value) == 0) {
                IMCoreManager.getMessageModule().processSessionByMessage(msg)
            }
        }
    }

    open fun getSessionDesc(msg: Message): String {
        return msg.content
    }

    /**
     * 图片压缩/视频抽帧等操作二次处理
     */
    open fun reprocessingFlowable(message: Message): Flowable<Message>? {
        return null
    }

    /**
     * 消息上传器
     */
    open fun uploadFlowable(entity: Message): Flowable<Message>? {
        return null
    }


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