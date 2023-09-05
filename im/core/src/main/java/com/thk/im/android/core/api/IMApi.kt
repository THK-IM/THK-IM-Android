package com.thk.im.android.core.api

import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import io.reactivex.Flowable

interface IMApi {

    /**
     * 获取修改时间为mTime之后的session列表
     */
    fun getLatestModifiedSessions(uId: Long, count: Int, mTime: Long): Flowable<List<Session>>

    /**
     * 获取与用户的session
     */
    fun querySession(uId: Long, sessionId: Long): Flowable<Session>

    /**
     * 创建会话
     */
    fun createSession(uId: Long, sessionType: Int, entityId: Long, members: Set<Long>?): Flowable<Session>

    /**
     * 发送消息到服务端
     */
    fun sendMessageToServer(msg: Message): Flowable<Message>

    /**
     * 消息设置ack
     */
    fun ackMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void>

    /**
     * 消息设置已读
     */
    fun readMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void>

    /**
     * 撤回消息
     */
    fun revokeMessage(uId: Long, sessionId: Long, msgId: Long): Flowable<Void>

    /**
     * 重新编辑消息
     */
    fun reeditMessage(uId: Long, sessionId: Long, msgId: Long, body: String): Flowable<Void>

    /**
     * 删除消息
     */
    fun deleteMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void>

    /**
     * 获取cTime之后创建的消息
     */
    fun getLatestMessages(uId: Long, cTime: Long, count: Int): Flowable<List<Message>>

}