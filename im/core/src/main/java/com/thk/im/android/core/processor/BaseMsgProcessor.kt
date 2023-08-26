package com.thk.im.android.core.processor

import androidx.annotation.WorkerThread
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.db.MsgOperateStatus
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

abstract class BaseMsgProcessor {

    /**
     * 创建发送消息
     */
    open fun buildSendMsg(
        body: String, sid: Long,
        atUsers: String? = null, rMsgId: Long? = null
    ): Message {
        val id = IMCoreManager.getMessageModule().generateNewMsgId()
        val oprStatus = MsgOperateStatus.Ack.value or
                MsgOperateStatus.ClientRead.value or
                MsgOperateStatus.ServerRead.value
        val sendStatus = MsgSendStatus.Init.value
        val type = this.messageType()
        val fUId = IMCoreManager.getUid()
        val cTime = IMCoreManager.getSignalModule().severTime
        val extData: String? = null
        // tips：msgId初始值给-id,发送成功后更新为服务端返回的msgId
        return Message(
            id, fUId, sid, 0 - id, type, body, oprStatus, sendStatus,
            cTime, cTime, extData, rMsgId, atUsers
        )
    }

    /**
     * 发送消息,逻辑流程:
     * 1、写入数据库,发送{新消息}通知更新ui
     * 2、如果有附件上传, 就上传, 附件上传完成后，更新本地数据库, 发送{消息更新}通知更新ui
     * 3、调用api发送消息到服务器,api调用结果更新本地数据库,发送{消息更新}通知更新ui
     */
    open fun sendMessage(
        body: String, sid: Long,
        atUsers: String? = null, rMsgId: Long? = null, map: Map<String, Any> = mutableMapOf()
    ) {
        val msg = buildSendMsg(body, sid, atUsers, rMsgId)
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                super.onComplete()
                msg.msgId = t.msgId
                msg.sendStatus = MsgSendStatus.SorRSuccess.value
                msg.cTime = t.cTime
                updateDb(msg)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                msg.sendStatus = MsgSendStatus.SendFailed.value
                msg.cTime = IMCoreManager.getSignalModule().severTime
                updateDb(msg)
            }
        }
        Flowable.create<Message>({
            try {
                insertDb(msg)
                it.onNext(msg)
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.LATEST).flatMap {
            it.sendStatus = MsgSendStatus.Sending.value
            updateDb(msg)
            val uploadFlowable = uploadFlowable(it)
            if (uploadFlowable == null) {
                return@flatMap Flowable.just(it)
            } else {
                return@flatMap uploadFlowable
            }
        }.flatMap {
            val msgModule = IMCoreManager.getMessageModule()
            msgModule.sendMessageToServer(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }

    /**
     * 重发
     */
    open fun resend(msg: Message) {
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                super.onComplete()
                msg.msgId = t.msgId
                msg.sendStatus = MsgSendStatus.SorRSuccess.value
                msg.cTime = t.cTime
                updateDb(msg)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                msg.sendStatus = MsgSendStatus.SendFailed.value
                msg.cTime = IMCoreManager.getSignalModule().severTime
                updateDb(msg)
            }
        }
        Flowable.just(msg).flatMap {
            it.sendStatus = MsgSendStatus.Sending.value
            updateDb(msg)
            val uploadFlowable = uploadFlowable(it)
            if (uploadFlowable == null) {
                return@flatMap Flowable.just(it)
            } else {
                return@flatMap uploadFlowable
            }
        }.flatMap {
            IMCoreManager.getMessageModule().sendMessageToServer(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }


    /**
     * 写db,有些消息除了插入本条记录，还需更新其他消息数据，如已读/已接收/撤回/评论
     */
    open fun insertDb(msg: Message) {
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.insertMessages(mutableListOf(msg))
        XEventBus.post(XEventType.MsgNew.value, msg)
    }

    /**
     * 更新db
     */
    open fun updateDb(msg: Message) {
        val msgDao = IMCoreManager.getImDataBase().messageDao()
        msgDao.updateMessages(mutableListOf(msg))
        XEventBus.post(XEventType.MsgUpdate.value, msg)
    }


    /**
     * 收到消息
     */
    @WorkerThread
    open fun received(msg: Message) {
        // 默认插入数据库
        val dbMsg = IMCoreManager.getImDataBase().messageDao().findMessage(msg.id)
        if (dbMsg == null) {
            insertDb(msg)
        }
    }

    open fun getSessionDesc(msg: Message): String {
        return msg.content
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
        return true
    }

    /**
     * 消息是否可以撤回
     */
    open fun canRevoke(msg: Message): Boolean {
        return false
    }

    /**
     * 消息类型, ex: 文本(1)
     */
    abstract fun messageType(): Int

}