package com.thk.im.android.live

import com.thk.im.android.live.api.vo.CallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CancelCallRoomMemberReqVo
import com.thk.im.android.live.api.vo.CreateRoomReqVo
import com.thk.im.android.live.api.vo.CreateRoomResVo
import com.thk.im.android.live.api.vo.DelRoomVo
import com.thk.im.android.live.api.vo.InviteMemberReqVo
import com.thk.im.android.live.api.vo.JoinRoomReqVo
import com.thk.im.android.live.api.vo.JoinRoomResVo
import com.thk.im.android.live.api.vo.KickoffMemberReqVo
import com.thk.im.android.live.api.vo.PlayStreamReqVo
import com.thk.im.android.live.api.vo.PlayStreamResVo
import com.thk.im.android.live.api.vo.PublishStreamReqVo
import com.thk.im.android.live.api.vo.PublishStreamResVo
import com.thk.im.android.live.api.vo.RefuseJoinRoomVo
import io.reactivex.Flowable
import retrofit2.http.Body

interface LiveApi {

    fun getEndpoint(): String

    fun publishStream(req: PublishStreamReqVo): Flowable<PublishStreamResVo>

    fun playStream(req: PlayStreamReqVo): Flowable<PlayStreamResVo>

    fun createRoom(req: CreateRoomReqVo): Flowable<CreateRoomResVo>

    fun callRoomMember(req: CallRoomMemberReqVo): Flowable<Void>

    fun cancelCallRoomMember( req: CancelCallRoomMemberReqVo): Flowable<Void>

    fun joinRoom(req: JoinRoomReqVo): Flowable<JoinRoomResVo>

    fun inviteMember(req: InviteMemberReqVo): Flowable<Void>

    fun refuseJoinRoom(req: RefuseJoinRoomVo): Flowable<Void>

    fun kickRoomMember(req: KickoffMemberReqVo): Flowable<Void>

    fun delRoom(req: DelRoomVo): Flowable<Void>
}