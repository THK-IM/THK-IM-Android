package com.thk.im.android.core.module

import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import io.reactivex.Flowable

interface MessageModule : CommonModule {

    /**
     * 注册消息处理器
     */
    fun registerMsgProcessor(processor: BaseMsgProcessor)

    /**
     * 获取注册消息处理器
     */
    fun getMsgProcessor(msgType: Int): BaseMsgProcessor

    /**
     * 同步离线消息
     * @param count 每次同步消息数量
     */
    fun syncOfflineMessages()

    /**
     * 同步最近session
     */
    fun syncLatestSessionsFromServer(lastSyncTime: Int, count: Int)

    /**
     * 创建entityId和sessionType对应的session, 先查本地数据库后查服务端
     */
    fun createSession(entityId: Long, sessionType: Int): Flowable<Session>

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
    fun deleteSession(sessionList: Array<Session>, deleteServer: Boolean): Flowable<Boolean>

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
        replyMsgId: Long? = null
    ): Boolean


    /**
     * 消息发送到服务端
     */
    fun sendMessageToServer(message: Message): Flowable<Message>

    /**
     * 标记消息已读
     */
    fun readMessages(sessionId: Long, msgIds: Set<Long>): Flowable<Boolean>

    /**
     * 撤回消息
     */
    fun revokeMessage(message: Message): Flowable<Boolean>


    /**
     * 重新编辑消息
     */
    fun reeditMessage(message: Message): Flowable<Boolean>

    /**
     * 消息ack:需要ack的消息存入客户端缓存,批量按sessionId进行ack
     */
    fun ackMessageToCache(sessionId: Long, msgId: Long)

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
    ): Flowable<Boolean>


    /**
     * 处理session
     */
    fun processSessionByMessage(msg: Message)

//    /**
//     * 新消息id
//     */
//    fun newMsgId(): Long
//
//    /**
//     * 获取消息处理器
//     */
//    fun getMessageProcessor(messageType: Int): BaseMsgProcessor
//
//    /**
//     * 消息ack
//     */
//    fun ackMessage(sid: Long, msgId: Long)
//
//    /**
//     * 检查本地ack发起
//     */
//    fun ackMessages()
//
//    /**
//     * 消息ack
//     */
//    fun ackMessages(sid: Long, msgIds: List<Long>)
//
//    /**
//     * 删除服务端消息
//     */
//    fun deleteServerMessages(sid: Long, msgIds: List<Long>): Flowable<Void>
//
//    /**
//     * 【用户主动发起】批量删除多条消息
//     */
//    fun deleteMessages(sid: Long, messages: List<Message>, deleteServer: Boolean): Flowable<Boolean>
//
//    /**
//     * 【收到服务器通知】 收到新消息
//     */
//    fun onNewMessage(bean: MessageBean)
//
//    /**
//     * 调用api发送消息到服务器
//     */
//    fun sendMessageToServer(bean: MessageBean): Flowable<MessageBean>
//
//    /**
//     * 【用户主动发起】 同步最近消息
//     */
//    fun syncLatestMessagesFromServer(
//        cTime: Long,
//        offset: Int,
//        size: Int
//    ): Flowable<List<MessageBean>>
//
//    /**
//     * 【系统连接成功后发起】
//     */
//    fun syncOfflineMessages(cTime: Long, offset: Int, size: Int)
//
//    /**
//     * 【用户主动发起】 同步所有消息
//     */
//    fun syncAllMessages(offset: Int, size: Int): Flowable<List<MessageBean>>
//
//    /**
//     * 【用户主动发起】同步最近session
//     */
//    fun syncLatestSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>>
//
//    /**
//     * 【用户主动发起】获取与某个用户的session
//     */
//    fun getSession(
//        uid: Long,
//        map: Map<String, Any> = mutableMapOf<String, Any>()
//    ): Flowable<Session>
//
//    /**
//     * 【用户主动发起】查询/创建服务器session
//     */
//    fun getSessionFromServerByEntityId(
//        entityId: Long,
//        type: Int,
//        map: Map<String, Any>
//    ): Flowable<SessionBean>
//
//    /**
//     * 【用户主动发起】查询session
//     */
//    fun querySessionFromServer(sid: Long): Flowable<SessionBean>
//
//
//    /**
//     * 【用户主动发起】查询session
//     */
//    fun queryLocalSession(sessionId: Long): Flowable<Session>
//
//    /**
//     * 【用户主动发起】同步所有session
//     */
//    fun syncAllSessionsFromServer(offset: Int, size: Int): Flowable<List<SessionBean>>
//
//
//    /**
//     * 【用户主动发起】分页获取本地session
//     */
//    fun queryLocalSessions(offset: Int, size: Int): Flowable<List<Session>>
//
//    /**
//     * 【用户主动发起】分页获取本地message
//     */
//    fun queryLocalMessages(sessionId: Long, cTime: Long, size: Int): Flowable<List<Message>>
//
//
//    /**
//     * 删除服务端会话
//     */
//    fun deleteServerSession(sessionList: List<Session>): Flowable<Int>
//
//
//    /**
//     * 删除本地会话
//     */
//    fun deleteLocalSession(sessionList: List<Session>): Flowable<Int>
//
//
//    /**
//     * 【用户主动发起】批量删除多条Session
//     */
//    fun deleteSession(sessionList: List<Session>, deleteServer: Boolean): Flowable<Boolean>
//
//
//    /**
//     * 标记session对应的所有消息为已读
//     */
//    fun signMessageReadBySessionId(sessionId: Long)
}