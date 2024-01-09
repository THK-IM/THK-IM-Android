package com.thk.im.android.core.api.internal

import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.api.vo.AckMsgVo
import com.thk.im.android.core.api.vo.DeleteMsgVo
import com.thk.im.android.core.api.vo.ForwardMessageVo
import com.thk.im.android.core.api.vo.MessageVo
import com.thk.im.android.core.api.vo.ReadMsgVo
import com.thk.im.android.core.api.vo.ReeditMsgVo
import com.thk.im.android.core.api.vo.RevokeMsgVo
import com.thk.im.android.core.api.vo.UpdateUserSession
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import io.reactivex.Flowable
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

open class DefaultIMApi(token: String, serverUrl: String) : IMApi {

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60

    private val interceptor = APITokenInterceptor(token)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(interceptor)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(serverUrl)
        .build()

    init {
        interceptor.addValidEndpoint(serverUrl)
    }

    private fun <T> getApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }

    private val messageApi: MessageApi = getApi(MessageApi::class.java)
    private val sessionApi: SessionApi = getApi(SessionApi::class.java)
    override fun queryLatestSessionMembers(
        sessionId: Long,
        mTime: Long,
        role: Int?,
        count: Int
    ): Flowable<List<SessionMember>> {
        return sessionApi.queryLatestSessionMembers(sessionId, mTime, role, count).flatMap { it ->
            val members = mutableListOf<SessionMember>()
            it.data.forEach { bean ->
                members.add(bean.toSessionMember())
            }
            Flowable.just(members)
        }
    }

    override fun queryUserLatestSessions(
        uId: Long,
        count: Int,
        mTime: Long,
        types: Set<Int>?
    ): Flowable<List<Session>> {
        return sessionApi.queryLatestSession(uId, mTime, 0, count, types).flatMap { it ->
            val sessions = mutableListOf<Session>()
            it.data.forEach { bean ->
                sessions.add(bean.toSession())
            }
            Flowable.just(sessions)
        }
    }

    override fun queryUserSession(uId: Long, entityId: Long, type: Int): Flowable<Session> {
        return sessionApi.querySessionByEntityId(uId, entityId, type).flatMap { bean ->
            Flowable.just(bean.toSession())
        }
    }

    override fun queryUserSession(uId: Long, sessionId: Long): Flowable<Session> {
        return sessionApi.querySession(uId, sessionId).flatMap { bean ->
            Flowable.just(bean.toSession())
        }
    }

    override fun deleteUserSession(uId: Long, session: Session): Flowable<Void> {
        return sessionApi.deleteSession(uId, session.id)
    }

    override fun updateUserSession(uId: Long, session: Session): Flowable<Void> {
        val updateUserSession = UpdateUserSession(
            uId,
            session.id,
            session.topTimestamp,
            session.status,
            session.parentId
        )
        return sessionApi.updateSession(updateUserSession)
    }

    override fun sendMessageToServer(msg: Message): Flowable<Message> {
        val req = MessageVo.buildMessageVo(msg)
        return messageApi.sendMsg(req).flatMap {
            msg.msgId = it.msgId
            msg.mTime = it.cTime
            msg.sendStatus = MsgSendStatus.Success.value
            msg.oprStatus = MsgOperateStatus.Ack.value or
                    MsgOperateStatus.ClientRead.value or
                    MsgOperateStatus.ServerRead.value
            Flowable.just(msg)
        }
    }

    override fun ackMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void> {
        val bean = AckMsgVo(sessionId, uId, msgIds)
        if (msgIds.isEmpty()) {
            return Flowable.empty()
        }
        return messageApi.ackMsg(bean)
    }

    override fun readMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void> {
        val bean = ReadMsgVo(sessionId, uId, msgIds)
        if (msgIds.isEmpty()) {
            return Flowable.empty()
        }
        return messageApi.readMsg(bean)
    }

    override fun revokeMessage(uId: Long, sessionId: Long, msgId: Long): Flowable<Void> {
        val bean = RevokeMsgVo(sessionId, uId, msgId)
        return messageApi.revokeMsg(bean)
    }

    override fun reeditMessage(
        uId: Long,
        sessionId: Long,
        msgId: Long,
        body: String?,
    ): Flowable<Void> {
        val bean = ReeditMsgVo(sessionId, uId, msgId, body)
        return messageApi.reeditMsg(bean)
    }

    override fun deleteMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Void> {
        val bean = DeleteMsgVo(sessionId, uId, msgIds)
        if (msgIds.isEmpty()) {
            return Flowable.empty()
        }
        return messageApi.deleteMessages(bean)
    }

    override fun forwardMessages(
        msg: Message,
        forwardSid: Long, fromUserIds: Set<Long>, clientMsgIds: Set<Long>
    ): Flowable<Message> {
        val bean = ForwardMessageVo.buildMessageBean(msg, forwardSid, fromUserIds, clientMsgIds)
        return messageApi.forwardMsg(bean).flatMap {
            msg.msgId = it.msgId
            msg.mTime = it.cTime
            msg.sendStatus = MsgSendStatus.Success.value
            msg.oprStatus = MsgOperateStatus.Ack.value or
                    MsgOperateStatus.ClientRead.value or
                    MsgOperateStatus.ServerRead.value
            Flowable.just(msg)
        }
    }

    override fun queryUserLatestMessages(uId: Long, cTime: Long, count: Int): Flowable<List<Message>> {
        return messageApi.queryLatestMsg(uId, cTime, 0, count).flatMap {
            val messages = mutableListOf<Message>()
            it.data.forEach { bean ->
                messages.add(bean.toMessage())
            }
            Flowable.just(messages)
        }
    }
}