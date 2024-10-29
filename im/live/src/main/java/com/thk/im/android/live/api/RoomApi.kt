package com.thk.im.android.live.api

import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.CreateRoomResVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.JoinRoomResVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface RoomApi {

    @POST("/room")
    fun createRoom(
        @Body req: CreateRoomReqVo
    ): Flowable<CreateRoomResVo>

    @POST("/room/call")
    fun callRoomMember(
        @Body req: CallRoomMemberReqVo
    ): Flowable<Void>

    @POST("/room/member/join")
    fun joinRoom(
        @Body req: JoinRoomReqVo
    ): Flowable<JoinRoomResVo>

    @POST("/room/member/invite")
    fun inviteMember(
        @Body req: InviteMemberReqVo
    ): Flowable<Void>

    @POST("/room/member/refuse_join")
    fun refuseJoinRoom(
        @Body req: RefuseJoinRoomVo
    ): Flowable<Void>


    @HTTP(method = "DELETE", path = "/room", hasBody = true)
    fun delRoom(
        @Body req: DelRoomVo
    ): Flowable<Void>
}