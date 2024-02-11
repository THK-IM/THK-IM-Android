package com.thk.android.im.live.api

import com.thk.android.im.live.vo.CreateRoomReqVo
import com.thk.android.im.live.vo.CreateRoomResVo
import com.thk.android.im.live.vo.DelRoomVo
import com.thk.android.im.live.vo.JoinRoomReqVo
import com.thk.android.im.live.vo.JoinRoomResVo
import com.thk.android.im.live.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import retrofit2.http.Body
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

    @POST("/room")
    fun delRoom(
        @Body req: DelRoomVo
    ): Flowable<Void>
}