package com.thk.android.im.live.bean

import com.google.gson.annotations.SerializedName

data class NotifyBean(
    @SerializedName("type")
    var type: String,
    @SerializedName("message")
    var message: String,
)

data class NewStreamNotify(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("uid")
    var uid: String,
    @SerializedName("stream_key")
    var streamKey: String,
)

data class RemoveStreamNotify(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("uid")
    var uid: String,
    @SerializedName("stream_key")
    var streamKey: String,
)