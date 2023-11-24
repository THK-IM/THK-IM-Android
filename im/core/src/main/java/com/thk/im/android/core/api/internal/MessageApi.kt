package com.thk.im.android.core.api.internal

import com.thk.im.android.core.api.bean.AckMsgBean
import com.thk.im.android.core.api.bean.DeleteMsgBean
import com.thk.im.android.core.api.bean.ForwardMessageBean
import com.thk.im.android.core.api.bean.ListBean
import com.thk.im.android.core.api.bean.MessageBean
import com.thk.im.android.core.api.bean.ReadMsgBean
import com.thk.im.android.core.api.bean.ReeditMsgBean
import com.thk.im.android.core.api.bean.RevokeMsgBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface MessageApi {

    /**
     * 发送消息
     */
    @POST("/message")
    fun sendMsg(
        @Body body: MessageBean
    ): Flowable<MessageBean>

    /**
     * 发送消息ack
     */
    @POST("/message/ack")
    fun ackMsg(
        @Body body: AckMsgBean
    ): Flowable<Void>

    /**
     * 发送消息read
     */
    @POST("/message/read")
    fun readMsg(
        @Body body: ReadMsgBean
    ): Flowable<Void>

    /**
     * 撤回消息
     */
    @POST("/message/revoke")
    fun revokeMsg(
        @Body body: RevokeMsgBean
    ): Flowable<Void>

    /**
     * 重新编辑消息
     */
    @POST("/message/reedit")
    fun reeditMsg(
        @Body body: ReeditMsgBean
    ): Flowable<Void>

    /**
     * 发送消息
     */
    @POST("/message/forward")
    fun forwardMsg(
        @Body body: ForwardMessageBean
    ): Flowable<ForwardMessageBean>

    /**
     * 查询最近消息
     */
    @GET("/message/latest")
    fun queryLatestMsg(
        @Query("u_id") uid: Long,
        @Query("c_time") cTime: Long,
        @Query("offset") offset: Int,
        @Query("count") count: Int
    ): Flowable<ListBean<MessageBean>>

    /**
     * 删除消息
     */
    @HTTP(method = "DELETE", path = "/message", hasBody = true)
    fun deleteMessages(
        @Body body: DeleteMsgBean
    ): Flowable<Void>

}