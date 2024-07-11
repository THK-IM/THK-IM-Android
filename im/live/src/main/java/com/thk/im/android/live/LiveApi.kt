package com.thk.im.android.live

import com.thk.im.android.live.vo.CreateRoomReqVo
import com.thk.im.android.live.vo.CreateRoomResVo
import com.thk.im.android.live.vo.DelRoomVo
import com.thk.im.android.live.vo.JoinRoomReqVo
import com.thk.im.android.live.vo.JoinRoomResVo
import com.thk.im.android.live.vo.PlayStreamReqVo
import com.thk.im.android.live.vo.PlayStreamResVo
import com.thk.im.android.live.vo.PublishStreamReqVo
import com.thk.im.android.live.vo.PublishStreamResVo
import com.thk.im.android.live.vo.RefuseJoinRoomVo
import io.reactivex.Flowable

interface LiveApi {

    fun getEndpoint(): String

    fun publishStream(req: PublishStreamReqVo): Flowable<PublishStreamResVo>

    fun playStream(req: PlayStreamReqVo): Flowable<PlayStreamResVo>

    fun createRoom(req: CreateRoomReqVo): Flowable<CreateRoomResVo>

    fun joinRoom(req: JoinRoomReqVo): Flowable<JoinRoomResVo>

    fun refuseJoinRoom(req: RefuseJoinRoomVo): Flowable<Void>

    fun delRoom(req: DelRoomVo): Flowable<Void>
}