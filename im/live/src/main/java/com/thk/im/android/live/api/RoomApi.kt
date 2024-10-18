package com.thk.im.android.live.api

import com.thk.im.android.live.vo.CreateRoomReqVo
import com.thk.im.android.live.vo.CreateRoomResVo
import com.thk.im.android.live.vo.DelRoomVo
import com.thk.im.android.live.vo.JoinRoomReqVo
import com.thk.im.android.live.vo.JoinRoomResVo
import com.thk.im.android.live.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface RoomApi {

    @POST("/room")
    fun createRoom(
        @Body req: CreateRoomReqVo
    ): Flowable<CreateRoomResVo>

    @POST("/room/member/join")
    fun joinRoom(
        @Body req: JoinRoomReqVo
    ): Flowable<JoinRoomResVo>

    @POST("/room/member/hangup")
    fun refuseJoinRoom(
        @Body req: RefuseJoinRoomVo
    ): Flowable<Void>


    @HTTP(method = "DELETE", path = "/room", hasBody = true)
    fun delRoom(
        @Body req: DelRoomVo
    ): Flowable<Void>
}