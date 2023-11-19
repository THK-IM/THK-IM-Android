package com.thk.im.android.core.module

import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import io.reactivex.Flowable

interface MessageModule : BaseModule {

    /**
     * 注册消息处理器
     */
    fun registerMsgProcessor(processor: IMBaseMsgProcessor)

    /**
     * 获取注册消息处理器
     */
    fun getMsgProcessor(msgType: Int): IMBaseMsgProcessor

    /**
     * 同步离线消息
     */
    fun syncOfflineMessages()

    /**
     * 同步最近session
     */
    fun syncLatestSessionsFromServer(lastSyncTime: Int, count: Int)

    /**
     *  先查本地数据库后查服务端
     */
    fun createSingleSession(entityId: Long): Flowable<Session>

    /**
     * 获取session, 先查本地数据库后查服务端
     */
    fun getSession(sessionId: Long): Flowable<Session>

    /**
     * 分页获取本地session
     */
    fun queryLocalSessions(count: Int, mTime: Long): Flowable<List<Session>>

    /**
     * 分页获取本地message
     */
    fun queryLocalMessages(sessionId: Long, cTime: Long, count: Int): Flowable<List<Message>>

    /**
     * 批量删除多条Session
     */
    fun deleteSession(session: Session, deleteServer: Boolean): Flowable<Void>

    /**
     * 更新session
     */
    fun updateSession(session: Session, updateServer: Boolean): Flowable<Void>

    /**
     * 收到新消息
     */
    fun onNewMessage(msg: Message)

    /**
     * 生成新消息id
     */
    fun generateNewMsgId(): Long

    /**
     * 发送消息
     */
    fun sendMessage(
        body: Any,
        sessionId: Long,
        type: Int,
        atUser: String? = null,
        replyMsgId: Long? = null,
        callback: IMSendMsgCallback? = null
    )

    /**
     * 重发
     */
    fun resend(msg: Message, callback: IMSendMsgCallback? = null)


    /**
     * 消息发送到服务端
     */
    fun sendMessageToServer(message: Message): Flowable<Message>

    /**
     * 消息ack:需要ack的消息存入客户端缓存,批量按sessionId进行ack
     */
    fun ackMessageToCache(message: Message)

    /**
     * 消息ack:发送到服务端
     */
    fun ackMessagesToServer()

    /**
     * 批量删除多条消息
     */
    fun deleteMessages(
        sessionId: Long,
        messages: List<Message>,
        deleteServer: Boolean
    ): Flowable<Void>

    /**
     * 处理session
     */
    fun processSessionByMessage(msg: Message)

    /**
     * session下有新消息，发出提示音/震动等通知
     */
    fun notifyNewMessage(session: Session, message: Message)

}