package com.thk.im.android.core.api

import com.thk.im.android.core.bean.AckMsgBean
import com.thk.im.android.core.bean.DeleteMsgBean
import com.thk.im.android.core.bean.ListBean
import com.thk.im.android.core.bean.MessageBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessageApi {

    /**
     * 发送消息
     */
    @POST("/im/v3/message")
    fun sendMsg(
        @Body body: MessageBean
    ): Flowable<MessageBean>

    /**
     * 发送消息ack
     */
    @POST("/im/v3/message/ack")
    fun ackMsg(
        @Body body: AckMsgBean
    ): Flowable<Void>

    /**
     * 查询最近消息
     */
    @GET("/im/v3/message/latest")
    fun queryLatestMsg(
        @Query("u_id") uid: Long,
        @Query("c_time") cTime: Long,
        @Query("offset") offset: Int,
        @Query("size") size: Int
    ): Flowable<ListBean<MessageBean>>

    /**
     * 发送消息ack
     */
    @DELETE("/im/v3/message")
    fun deleteMessages(
        @Body body: DeleteMsgBean
    ): Flowable<Void>

}