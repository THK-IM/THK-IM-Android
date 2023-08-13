package com.thk.android.im.live.bean

import com.google.gson.annotations.SerializedName

data class PlayReqBean(
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("offer_sdp")
    val offerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)

data class PlayResBean(
    @SerializedName("answer_sdp")
    val answerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)