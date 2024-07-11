package com.thk.im.android.live.vo

import com.google.gson.annotations.SerializedName

data class PublishStreamReqVo(
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("offer_sdp")
    val offerSdp: String,
)

data class PublishStreamResVo(
    @SerializedName("answer_sdp")
    val answerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)