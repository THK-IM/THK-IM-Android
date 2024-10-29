package com.thk.im.android.live.api

import com.thk.im.android.live.api.vo.PlayStreamReqVo
import com.thk.im.android.live.api.vo.PlayStreamResVo
import com.thk.im.android.live.api.vo.PublishStreamReqVo
import com.thk.im.android.live.api.vo.PublishStreamResVo
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