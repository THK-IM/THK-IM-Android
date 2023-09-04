package com.thk.im.android.core.api.internal

import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.api.bean.AckMsgBean
import com.thk.im.android.core.api.bean.CreateSessionBean
import com.thk.im.android.core.api.bean.DeleteMsgBean
import com.thk.im.android.core.api.bean.MessageBean
import com.thk.im.android.core.api.bean.ReadMsgBean
import com.thk.im.android.core.api.bean.ReeditMsgBean
import com.thk.im.android.core.api.bean.RevokeMsgBean
import com.thk.im.android.db.MsgOperateStatus
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.SessionType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import io.reactivex.Flowable
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DefaultIMApi(serverUrl: String, token: String) : IMApi {

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60

    private val interceptor = TokenInterceptor(token)

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

    private fun <T> getApi(cls: Class<T>): T {
        return retrofit.create(cls)
    }

    private val messageApi: MessageApi = getApi(MessageApi::class.java)
    private val sessionApi: SessionApi = getApi(SessionApi::class.java)

    override fun getLatestModifiedSessions(
        uId: Long,
        count: Int,
        mTime: Long
    ): Flowable<List<Session>> {
        return sessionApi.queryLatestSession(uId, mTime, 0, count).flatMap { it ->
            val sessions = mutableListOf<Session>()
            it.data.forEach { bean ->
                sessions.add(bean.toSession())
            }
            Flowable.just(sessions)
        }
    }

    override fun querySession(uId: Long, sessionId: Long): Flowable<Session> {
        return sessionApi.querySession(uId, sessionId).flatMap { bean ->
            Flowable.just(bean.toSession())
        }
    }


    override fun createSession(
        uId: Long,
        sessionType: Int,
        entityId: Long,
        members: Set<Long>?
    ): Flowable<Session> {
        val bean = CreateSessionBean(uId, sessionType, entityId, members)
        return sessionApi.createSession(bean).flatMap {
            Flowable.just(it.toSession())
        }
    }

    override fun sendMessageToServer(msg: Message): Flowable<Message> {
        val bean = MessageBean.buildMessageBean(msg)
        return messageApi.sendMsg(bean).flatMap {
            msg.msgId = it.msgId
            msg.cTime = it.cTime
            msg.sendStatus = MsgSendStatus.Success.value
            msg.oprStatus = MsgOperateStatus.Ack.value or
                    MsgOperateStatus.ClientRead.value or
                    MsgOperateStatus.ServerRead.value
            Flowable.just(msg)
        }
    }

    override fun ackMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val bean = AckMsgBean(sessionId, uId, msgIds)
        return messageApi.ackMsg(bean).flatMap {
            Flowable.just(true)
        }
    }

    override fun readMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val bean = ReadMsgBean(sessionId, uId, msgIds)
        return messageApi.readMsg(bean).flatMap {
            Flowable.just(true)
        }
    }

    override fun revokeMessage(uId: Long, sessionId: Long, msgId: Long): Flowable<Boolean> {
        val bean = RevokeMsgBean(sessionId, uId, msgId)
        return messageApi.revokeMsg(bean).flatMap {
            Flowable.just(true)
        }
    }

    override fun reeditMessage(
        uId: Long,
        sessionId: Long,
        msgId: Long,
        body: String
    ): Flowable<Boolean> {
        val bean = ReeditMsgBean(sessionId, uId, msgId, body)
        return messageApi.reeditMsg(bean).flatMap {
            Flowable.just(true)
        }
    }

    override fun deleteMessages(uId: Long, sessionId: Long, msgIds: Set<Long>): Flowable<Boolean> {
        val bean = DeleteMsgBean(sessionId, uId, msgIds)
        return messageApi.deleteMessages(bean).flatMap {
            Flowable.just(true)
        }
    }

    override fun getLatestMessages(uId: Long, cTime: Long, count: Int): Flowable<List<Message>> {
        return messageApi.queryLatestMsg(uId, cTime, 0, count).flatMap {
            val messages = mutableListOf<Message>()
            it.data.forEach { bean ->
                messages.add(bean.toMessage())
            }
            Flowable.just(messages)
        }
    }
}