package com.thk.android.im.live

import com.thk.android.im.live.vo.CreateRoomReqVo
import com.thk.android.im.live.vo.CreateRoomResVo
import com.thk.android.im.live.vo.JoinRoomReqVo
import com.thk.android.im.live.vo.JoinRoomResVo
import com.thk.android.im.live.vo.PlayStreamReqVo
import com.thk.android.im.live.vo.PlayStreamResVo
import com.thk.android.im.live.vo.PublishStreamReqVo
import com.thk.android.im.live.vo.PublishStreamResVo
import io.reactivex.Flowable

interface LiveApi {

    fun getEndpoint(): String

    fun publishStream(req: PublishStreamReqVo): Flowable<PublishStreamResVo>

    fun playStream(req: PlayStreamReqVo): Flowable<PlayStreamResVo>

    fun createRoom(req: CreateRoomReqVo): Flowable<CreateRoomResVo>

    fun joinRoom(req: JoinRoomReqVo): Flowable<JoinRoomResVo>
}