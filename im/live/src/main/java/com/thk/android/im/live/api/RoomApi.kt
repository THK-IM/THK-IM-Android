package com.thk.android.im.live.api

import com.thk.android.im.live.bean.CreateRoomReqBean
import com.thk.android.im.live.bean.CreateRoomResBean
import com.thk.android.im.live.bean.JoinRoomReqBean
import com.thk.android.im.live.bean.JoinRoomResBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomApi {

    @POST("/room")
    fun createRoom(
        @Body bean: CreateRoomReqBean
    ): Flowable<CreateRoomResBean>

    @POST("/room/join")
    fun joinRoom(
        @Body bean: JoinRoomReqBean
    ): Flowable<JoinRoomResBean>
}