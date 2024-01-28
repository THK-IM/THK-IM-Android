package com.thk.android.im.live.vo

import com.google.gson.annotations.SerializedName

data class PlayStreamReqVo(
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("offer_sdp")
    val offerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)

data class PlayStreamResVo(
    @SerializedName("answer_sdp")
    val answerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)