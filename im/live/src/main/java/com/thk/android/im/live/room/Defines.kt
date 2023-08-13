package com.thk.android.im.live.room

import com.google.gson.annotations.SerializedName


enum class Role(value: Int) {
    Audience(1), // 观众
    Broadcaster(2), //
}

enum class Mode(value: Int) {
    Chat(0), // 文字直播间
    Audio(1), // 视频直播间
    Video(2), // 视频直播间
}

data class Member(
    @SerializedName("uid")
    var uid: String,
    @SerializedName("role")
    var role: Int,
    @SerializedName("join_time")
    var joinTime: Long,
    @SerializedName("stream_key")
    var streamKey: String?,
)
