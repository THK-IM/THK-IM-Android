package com.thk.android.im.live.api

import com.thk.android.im.live.bean.PlayReqBean
import com.thk.android.im.live.bean.PlayResBean
import com.thk.android.im.live.bean.PublishReqBean
import com.thk.android.im.live.bean.PublishResBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface RtcApi {

    @POST("/stream/publish")
    fun requestPublish(
        @Body bean: PublishReqBean
    ): Flowable<PublishResBean>


    @POST("/stream/play")
    fun requestPlay(
        @Body bean: PlayReqBean
    ): Flowable<PlayResBean>


}