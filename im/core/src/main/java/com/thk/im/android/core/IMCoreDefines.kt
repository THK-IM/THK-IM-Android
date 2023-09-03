package com.thk.im.android.core

import com.google.gson.annotations.SerializedName


/**
 * IM事件
 */
enum class IMEvent(val value: String) {
    OnlineStatusUpdate("IMEventOnlineStatusUpdate"),
    BatchMsgNew("IMEventBatchMsgNew"),
    MsgNew("IMEventMsgNew"),
    MsgUpdate("IMEventMsgUpdate"),
    MsgDelete("IMEventOnlineStatusUpdate"),
    MsgUploadProgressUpdate("IMEventMsgUploadProgressUpdate"),
    SessionNew("IMEventSessionNew"),
    SessionUpdate("IMEventSessionUpdate"),
    SessionDelete("IMEventSessionDelete"),
}

enum class IMFileFormat(val value: String) {
    Image("image"),
    Video("video"),
    Audio("audio"),
    Doc("Doc"),
    Other("other"),
}

data class IMUploadProgress(
    @SerializedName("key")
    var key: String,
    @SerializedName("state")
    var state: Int,
    @SerializedName("progress")
    var progress: Int
)
