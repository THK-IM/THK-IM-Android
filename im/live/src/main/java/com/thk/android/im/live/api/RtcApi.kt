package com.thk.android.im.live.api

import com.thk.android.im.live.vo.PlayStreamReqVo
import com.thk.android.im.live.vo.PlayStreamResVo
import com.thk.android.im.live.vo.PublishStreamReqVo
import com.thk.android.im.live.vo.PublishStreamResVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface RtcApi {

    @POST("/stream/publish")
    fun requestPublish(
        @Body req: PublishStreamReqVo
    ): Flowable<PublishStreamResVo>


    @POST("/stream/play")
    fun requestPlay(
        @Body req: PlayStreamReqVo
    ): Flowable<PlayStreamResVo>


}