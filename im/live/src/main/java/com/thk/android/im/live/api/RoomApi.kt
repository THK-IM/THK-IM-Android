package com.thk.android.im.live.api

import com.thk.android.im.live.vo.CreateRoomReqVo
import com.thk.android.im.live.vo.CreateRoomResVo
import com.thk.android.im.live.vo.JoinRoomReqVo
import com.thk.android.im.live.vo.JoinRoomResVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomApi {

    @POST("/room")
    fun createRoom(
        @Body req: CreateRoomReqVo
    ): Flowable<CreateRoomResVo>

    @POST("/room/join")
    fun joinRoom(
        @Body req: JoinRoomReqVo
    ): Flowable<JoinRoomResVo>
}