package com.thk.android.im.live.bean

import com.google.gson.annotations.SerializedName

data class PublishReqBean(
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("offer_sdp")
    val offerSdp: String,
)

data class PublishResBean(
    @SerializedName("answer_sdp")
    val answerSdp: String,
    @SerializedName("stream_key")
    val streamKey: String,
)