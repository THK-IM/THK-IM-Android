package com.thk.im.android.live.api

import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CancelCallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.JoinRoomResVo
import com.thk.im.android.live.api.vo.KickoffMemberReqVo
import com.thk.im.android.live.api.vo.LeaveRoomReqVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import com.thk.im.android.live.api.vo.RoomResVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomApi {

    @POST("/room")
    fun createRoom(
        @Body req: CreateRoomReqVo
    ): Flowable<RoomResVo>

    @GET("/room/{id}")
    fun queryRoom(
        @Path("id") id: String
    ): Flowable<RoomResVo>

    @POST("/room/call")
    fun callRoomMember(
        @Body req: CallRoomMemberReqVo
    ): Flowable<Void>

    @POST("/room/cancel_call")
    fun cancelCallRoomMember(
        @Body req: CancelCallRoomMemberReqVo
    ): Flowable<Void>

    @POST("/room/member/join")
    fun joinRoom(
        @Body req: JoinRoomReqVo
    ): Flowable<JoinRoomResVo>

    @POST("/room/member/leave")
    fun leaveRoom(
        @Body req: LeaveRoomReqVo
    ): Flowable<Void>

    @POST("/room/member/invite")
    fun inviteMember(
        @Body req: InviteMemberReqVo
    ): Flowable<Void>

    @POST("/room/member/refuse_join")
    fun refuseJoinRoom(
        @Body req: RefuseJoinRoomVo
    ): Flowable<Void>

    @POST("/room/member/kick")
    fun kickoffMember(
        @Body req: KickoffMemberReqVo
    ): Flowable<Void>


    @HTTP(method = "DELETE", path = "/room", hasBody = true)
    fun delRoom(
        @Body req: DelRoomVo
    ): Flowable<Void>
}