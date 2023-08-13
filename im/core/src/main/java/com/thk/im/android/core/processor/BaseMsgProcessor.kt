package com.thk.im.android.core.processor

import androidx.annotation.WorkerThread
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.api.BaseSubscriber
import com.thk.im.android.core.api.RxTransform
import com.thk.im.android.core.bean.MessageBean
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.MsgStatus
import com.thk.im.android.db.entity.Session
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.json.JSONObject

abstract class BaseMsgProcessor {

    /**
     * 创建发送消息
     */
    open fun buildSendMsg(
        body: String, sid: Long,
        atUsers: String? = null, rMsgId: Long? = null
    ): Message {
        val id = IMManager.getMessageModule().newMsgId()
        val status = MsgStatus.Init
        val type = this.messageType()
        val fUId = IMManager.getUid()
        val cTime = IMManager.getSignalModule().severTime
        val extData: String? = null
        // tips：msgId初始值给-id,发送成功后更新为服务端返回的msgId
        return Message(
            id, fUId, sid, 0 - id, type, body, status.value,
            extData, rMsgId, atUsers, cTime, cTime
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
        val subscriber = object : BaseSubscriber<MessageBean>() {
            override fun onNext(t: MessageBean) {
                super.onComplete()
                msg.msgId = t.msgId
                msg.status = MsgStatus.SorRSuccess.value
                msg.cTime = t.cTime
                // 金币数量保存到ext_data里面(目前只能先这么存储，后期想办法优化)
                // 单聊返回coins字段，群聊不返回coin字段，默认值为-1
                if (t.coins > -1) {
                    msg.extData = JSONObject().put("coins", t.coins).toString()
                }
                onMessageSendResult(msg)
                onSessionNewMessage(msg)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                msg.status = MsgStatus.SendFailed.value
                msg.cTime = IMManager.getSignalModule().severTime
                onMessageSendResult(msg)
                onSessionNewMessage(msg)
                // FileNotFound/UploadException/HttpException
                XEventBus.post(XEventType.MsgSendFailed.value, t)
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
            it.status = MsgStatus.Sending.value
            updateDb(msg)
            val uploadFlowable = uploadFlowable(it)
            if (uploadFlowable == null) {
                return@flatMap Flowable.just(it)
            } else {
                return@flatMap uploadFlowable
            }
        }.flatMap {
            val bean = entity2MsgBean(it)
            val msgModule = IMManager.getMessageModule()
            msgModule.sendMessageToServer(bean)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }

    /**
     * 重发
     */
    open fun resend(msg: Message) {
        val subscriber = object : BaseSubscriber<MessageBean>() {
            override fun onNext(t: MessageBean) {
                super.onComplete()
                msg.msgId = t.msgId
                msg.status = MsgStatus.SorRSuccess.value
                msg.cTime = t.cTime
                onMessageSendResult(msg)
                onSessionNewMessage(msg)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                msg.status = MsgStatus.SendFailed.value
                msg.cTime = IMManager.getSignalModule().severTime
                onMessageSendResult(msg)
                onSessionNewMessage(msg)
                // FileNotFound/UploadException/HttpException
                XEventBus.post(XEventType.MsgSendFailed.value, t)
            }
        }
        Flowable.just(msg).flatMap {
            it.status = MsgStatus.Sending.value
            updateDb(msg)
            val uploadFlowable = uploadFlowable(it)
            if (uploadFlowable == null) {
                return@flatMap Flowable.just(it)
            } else {
                return@flatMap uploadFlowable
            }
        }.flatMap {
            val bean = entity2MsgBean(it)
            IMManager.getMessageModule().sendMessageToServer(bean)
        }.compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }

    /**
     * 与服务器交互的bean转换为本地数据entity
     */
    open fun msgBean2Entity(bean: MessageBean): Message {
        return Message(
            bean.clientId, bean.fUId, bean.sessionId, bean.msgId,
            bean.type, bean.body, MsgStatus.SorRSuccess.value, null,
            bean.rMsgId, bean.atUsers, bean.cTime, bean.cTime
        )
    }

    /**
     * 本地数据entity转换为与服务器交互的bean
     */
    open fun entity2MsgBean(msg: Message): MessageBean {
        return MessageBean(
            msg.id, msg.fUid, msg.sid, msg.msgId, msg.type,
            msg.content, msg.atUsers, msg.rMsgId, msg.cTime
        )
    }

    /**
     * 写db,有些消息除了插入本条记录，还需更新其他消息数据，如已读/已接收/撤回/评论
     */
    open fun insertDb(msg: Message) {
        val msgDao = IMManager.getImDataBase().messageDao()
        msgDao.insertMessages(msg)
        XEventBus.post(XEventType.MsgNew.value, msg)
    }

    /**
     * 更新db
     */
    open fun updateDb(msg: Message) {
        val msgDao = IMManager.getImDataBase().messageDao()
        msgDao.updateMessages(msg)
        XEventBus.post(XEventType.MsgUpdate.value, msg)
    }

    /**
     * 更新数据库的msg_id和状态
     */
    open fun onMessageSendResult(msg: Message) {
        val msgDao = IMManager.getImDataBase().messageDao()
        msgDao.updateMessageState(msg.id, msg.status, msg.msgId, msg.extData.orEmpty(), msg.cTime)
        val dbMsg = msgDao.findMessage(msg.id)
        XEventBus.post(XEventType.MsgUpdate.value, dbMsg)
    }

    /**
     * 更新消息content
     */
    open fun updateMsgContent(msg: Message, sendNotify: Boolean = true) {
        val msgDao = IMManager.getImDataBase().messageDao()
        msgDao.updateMessageContent(msg.id, msg.content)
        if (sendNotify) {
            val dbMsg = msgDao.findMessage(msg.id)
            XEventBus.post(XEventType.MsgUpdate.value, dbMsg)
        }
    }

    /**
     * 收到消息
     */
    @WorkerThread
    open fun received(bean: MessageBean) {
        // 默认插入数据库
        val message = msgBean2Entity(bean)
        val dbMsg = IMManager.getImDataBase().messageDao().findMessage(message.id)
        if (dbMsg == null) {
            insertDb(message)
            onSessionNewMessage(message)
            IMManager.getMessageModule().ackMessage(message.sid, message.msgId)
        }
    }

    open fun getSessionDesc(msg: Message): String {
        return msg.content
    }

    open fun deleteMessage(msg: Message) {
        IMManager.getImDataBase().messageDao().deleteMessages(msg)
        XEventBus.post(XEventType.MsgDeleted.value, msg)
    }

    /**
     * 删除单条消息
     */
    fun deleteSingleMessage(msg: Message, deleteServer: Boolean = false): Flowable<Boolean> {
        return if (deleteServer) {
            val messageModule = IMManager.getMessageModule()
            messageModule.deleteServerMessages(msg.sid, listOf(msg.msgId)).flatMap {
                Flowable.create({
                    deleteMessage(msg)
                }, BackpressureStrategy.LATEST)
            }
        } else {
            Flowable.create({
                deleteMessage(msg)
            }, BackpressureStrategy.LATEST)
        }
    }

    /**
     * 入库新消息后触发session变更
     */
    open fun onSessionNewMessage(message: Message) {
        val messageModule = IMManager.getMessageModule()
        val messageDao = IMManager.getImDataBase().messageDao()
        val sessionDao = IMManager.getImDataBase().sessionDao()
        val subscribe = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session) {
                super.onComplete()
                t.lastMsg = getSessionDesc(message)
                t.mTime = message.cTime
                t.unRead = messageDao.getUnReadCount(t.id, IMManager.getUid())
                sessionDao.updateSession(t)
                XEventBus.post(XEventType.SessionNew.value, t)
            }
        }
        messageModule.queryLocalSession(message.sid).flatMap {
            if (it.entityId == 0L) {
                return@flatMap messageModule.querySessionFromServer(it.id).flatMap { bean ->
                    val session = bean.toSession()
                    sessionDao.insertSessions(session)
                    Flowable.just(session)
                }
            }
            return@flatMap Flowable.just(it)
        }.compose(RxTransform.flowableToIo()).subscribe(subscribe)
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