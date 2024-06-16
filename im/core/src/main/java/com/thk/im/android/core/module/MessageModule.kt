package com.thk.im.android.core.module

import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
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
    fun syncLatestSessionsFromServer()


    /**
     * 同步超级群消息
     */
    fun syncSuperGroupMessages()

    /**
     * 获取session, 先查本地数据库后查服务端
     */
    fun getSession(entityId: Long, type: Int): Flowable<Session>

    /**
     * 获取session, 先查本地数据库后查服务端
     */
    fun getSession(sessionId: Long): Flowable<Session>

    /**
     * 分页获取本地session
     */
    fun queryLocalSessions(parentId: Long, count: Int, mTime: Long): Flowable<List<Session>>

    /**
     * 分页获取本地message
     */
    fun queryLocalMessages(
        sessionId: Long,
        startTime: Long,
        endTime: Long,
        count: Int
    ): Flowable<List<Message>>

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
        sessionId: Long,
        type: Int,
        body: Any?,
        data: Any?,
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
     * 删除本地消息
     */
    fun deleteAllLocalSessionMessage(session: Session): Flowable<Void>

    /**
     * 处理session
     */
    fun processSessionByMessage(msg: Message, forceNotify: Boolean = false)

    /**
     * session下有新消息，发出提示音/震动等通知
     */
    fun notifyNewMessage(session: Session, message: Message)

    /**
     * 查询session下的成员列表
     */
    fun querySessionMembers(sessionId: Long, forceServer: Boolean): Flowable<List<SessionMember>>

    /**
     * 同步session成员列表
     */
    fun syncSessionMembers(sessionId: Long)

}