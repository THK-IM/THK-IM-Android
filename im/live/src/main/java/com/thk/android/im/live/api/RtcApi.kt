package com.thk.android.im.live.api

import com.thk.android.im.live.bean.CreateRoomReqBean
import com.thk.android.im.live.bean.CreateRoomResBean
import com.thk.android.im.live.bean.JoinRoomReqBean
import com.thk.android.im.live.bean.JoinRoomResBean
import com.thk.android.im.live.bean.PlayReqBean
import com.thk.android.im.live.bean.PlayResBean
import com.thk.android.im.live.bean.PublishReqBean
import com.thk.android.im.live.bean.PublishResBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface RtcApi {

    @POST("/room")
    fun createRoom(
        @Body bean: CreateRoomReqBean
    ): Flowable<CreateRoomResBean>

    @POST("/room/join")
    fun joinRoom(
        @Body bean: JoinRoomReqBean
    ): Flowable<JoinRoomResBean>

    @POST("/stream/publish")
    fun requestPublish(
        @Body bean: PublishReqBean
    ): Flowable<PublishResBean>


    @POST("/stream/play")
    fun requestPlay(
        @Body bean: PlayReqBean
    ): Flowable<PlayResBean>



}