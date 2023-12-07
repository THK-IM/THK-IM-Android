package com.thk.im.android.core.api.internal

import com.thk.im.android.core.api.vo.AckMsgVo
import com.thk.im.android.core.api.vo.DeleteMsgVo
import com.thk.im.android.core.api.vo.ForwardMessageVo
import com.thk.im.android.core.api.vo.ListVo
import com.thk.im.android.core.api.vo.MessageVo
import com.thk.im.android.core.api.vo.ReadMsgVo
import com.thk.im.android.core.api.vo.ReeditMsgVo
import com.thk.im.android.core.api.vo.RevokeMsgVo
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
        @Body body: MessageVo
    ): Flowable<MessageVo>

    /**
     * 发送消息ack
     */
    @POST("/message/ack")
    fun ackMsg(
        @Body body: AckMsgVo
    ): Flowable<Void>

    /**
     * 发送消息read
     */
    @POST("/message/read")
    fun readMsg(
        @Body body: ReadMsgVo
    ): Flowable<Void>

    /**
     * 撤回消息
     */
    @POST("/message/revoke")
    fun revokeMsg(
        @Body body: RevokeMsgVo
    ): Flowable<Void>

    /**
     * 重新编辑消息
     */
    @POST("/message/reedit")
    fun reeditMsg(
        @Body body: ReeditMsgVo
    ): Flowable<Void>

    /**
     * 发送消息
     */
    @POST("/message/forward")
    fun forwardMsg(
        @Body body: ForwardMessageVo
    ): Flowable<ForwardMessageVo>

    /**
     * 查询最近消息
     */
    @GET("/message/latest")
    fun queryLatestMsg(
        @Query("u_id") uid: Long,
        @Query("c_time") cTime: Long,
        @Query("offset") offset: Int,
        @Query("count") count: Int
    ): Flowable<ListVo<MessageVo>>

    /**
     * 删除消息
     */
    @HTTP(method = "DELETE", path = "/message", hasBody = true)
    fun deleteMessages(
        @Body body: DeleteMsgVo
    ): Flowable<Void>

}