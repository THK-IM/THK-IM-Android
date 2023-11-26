package com.thk.im.android.core.processor

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

abstract class IMBaseMsgProcessor {

    protected val downLoadingUrls = mutableListOf<String>()
    protected val disposables = CompositeDisposable()

    /**
     * 收到消息
     */
    @WorkerThread
    open fun received(msg: Message) {
        // 默认插入数据库
        val dbMsg =
            IMCoreManager.getImDataBase().messageDao().findMessageById(msg.id, msg.fUid, msg.sid)
        if (dbMsg == null) {
            if (msg.fUid == IMCoreManager.uId) {
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
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.uId) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
        } else {
            if (dbMsg.sendStatus != MsgSendStatus.Success.value) {
                msg.data = dbMsg.data
                msg.sendStatus = MsgSendStatus.Success.value
                if (msg.fUid == IMCoreManager.uId) {
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
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.uId) {
                IMCoreManager.getMessageModule().ackMessageToCache(msg)
            }
        }
    }

    /**
     * 创建发送消息
     */
    open fun buildSendMsg(
        sid: Long, content: Any? = null, data: Any? = null,
        atUsers: String? = null, rMsgId: Long? = null
    ): Message {
        val dbContent = when (content) {
            null -> {
                null
            }

            is String -> {
                content
            }

            else -> {
                Gson().toJson(content)
            }
        }
        val dbData = when (data) {
            null -> {
                null
            }

            is String -> {
                data
            }

            else -> {
                Gson().toJson(data)
            }
        }
        val id = IMCoreManager.getMessageModule().generateNewMsgId()
        val oprStatus =
            MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
        val sendStatus = MsgSendStatus.Init.value
        val type = this.messageType()
        val fUId = IMCoreManager.uId
        val cTime = IMCoreManager.getCommonModule().getSeverTime()
        // tips：msgId初始值给-id,发送成功后更新为服务端返回的msgId
        return Message(
            id,
            fUId,
            sid,
            0 - id,
            type,
            dbContent,
            dbData,
            sendStatus,
            oprStatus,
            null,
            rMsgId,
            atUsers,
            null,
            cTime,
            cTime,
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
        sid: Long,
        body: Any?,
        data: Any?,
        atUsers: String? = null,
        rMsgId: Long? = null,
        callback: IMSendMsgCallback? = null
    ) {
        val msg = buildSendMsg(sid, body, data, atUsers, rMsgId)
        this.send(msg, false, callback)
    }

    open fun resend(msg: Message, callback: IMSendMsgCallback? = null) {
        send(msg, true, callback)
    }

    /**
     * 重发
     */
    open fun send(msg: Message, resend: Boolean = false, callback: IMSendMsgCallback? = null) {
        var originMsg = msg
        val subscriber = object : BaseSubscriber<Message>() {

            override fun onNext(t: Message) {
                insertOrUpdateDb(
                    msg,
                    notify = true,
                    notifySession = true,
                )
                callback?.onResult(t, null)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                LLog.e("Message Send err $t")
                originMsg.sendStatus = MsgSendStatus.SendFailed.value
                updateFailedMsgStatus(originMsg)
                callback?.onResult(originMsg, Exception(t))
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        Flowable.just(originMsg).flatMap {
            if (!resend) {
                insertOrUpdateDb(
                    originMsg,
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
                    it,
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
            return@flatMap sendToServer(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    open fun sendToServer(message: Message): Flowable<Message> {
        return IMCoreManager.getMessageModule().sendMessageToServer(message)
    }

    open fun forwardMessage(msg: Message, sid: Long, callback: IMSendMsgCallback? = null) {
        val oldSessionId = msg.sid
        val oldMsgClientId = msg.id
        val oldFromUserId = msg.fUid
        val forwardMessage = msg.copy()
        forwardMessage.id = IMCoreManager.getMessageModule().generateNewMsgId()
        forwardMessage.fUid = IMCoreManager.uId
        forwardMessage.sid = sid
        forwardMessage.oprStatus =
            MsgOperateStatus.Ack.value or MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
        forwardMessage.sendStatus = MsgSendStatus.Init.value
        forwardMessage.cTime = IMCoreManager.getCommonModule().getSeverTime()
        forwardMessage.mTime = forwardMessage.cTime

        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                insertOrUpdateDb(t)
                callback?.onResult(t, null)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                callback?.onResult(forwardMessage, Exception(t))
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        IMCoreManager.imApi.forwardMessages(
            forwardMessage,
            oldSessionId,
            setOf(oldFromUserId),
            setOf(oldMsgClientId)
        )
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
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
    open fun downloadMsgContent(entity: Message, resourceType: String): Boolean {
        return true
    }


    /**
     * 消息是否在界面上显示，撤回/已读/已接受等状态消息不显示
     */
    open fun isShow(msg: Message): Boolean {
        return msg.type >= 0
    }

    /**
     * 消息是否需要二次处理，用于拉取同步消息时，不需要二次处理的消息批量入库，需要二次处理的消息单独处理
     */
    open fun needReprocess(msg: Message): Boolean {
        return false
    }


    /**
     * 消息类型, ex: 文本(1)
     */
    abstract fun messageType(): Int

}