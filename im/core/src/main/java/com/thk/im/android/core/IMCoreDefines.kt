package com.thk.im.android.core

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Message


/**
 * 信令状态
 */
@Keep
enum class SignalStatus(val value: Int) {
    Init(0),
    Connecting(1),
    Connected(2),
    Disconnected(3)
}


/**
 * session状态
 */
@Keep
enum class SessionStatus(val value: Int) {
    Reject(1),
    Silence(2),
}

/**
 * IM事件
 */
@Keep
enum class IMEvent(val value: String) {
    OnlineStatusUpdate("IMEventOnlineStatusUpdate"),
    BatchMsgNew("IMEventBatchMsgNew"),
    MsgNew("IMEventMsgNew"),
    MsgUpdate("IMEventMsgUpdate"),
    MsgDelete("IMEventMsgDelete"),
    BatchMsgDelete("IMEventBatchMsgDelete"),
    SessionMessageClear("IMEventSessionMessageClear"),
    SessionNew("IMEventSessionNew"),
    SessionUpdate("IMEventSessionUpdate"),
    SessionDelete("IMEventSessionDelete"),
    MsgLoadStatusUpdate("IMEventMsgLoadStatusUpdate"),
}

@Keep
enum class IMFileFormat(val value: String) {
    Image("image"),
    Video("video"),
    Audio("audio"),
    Doc("Doc"),
    Other("other"),
}

@Keep
data class IMLoadProgress(
    @SerializedName("type")
    var type: String,
    @SerializedName("url")
    var url: String,
    @SerializedName("path")
    var path: String,
    @SerializedName("state")
    var state: Int,
    @SerializedName("progress")
    var progress: Int
)

@Keep
enum class IMLoadType(val value: String) {
    Upload("upload"),
    Download("download"),
}

@Keep
enum class IMMsgResourceType(val value: String) {
    Thumbnail("thumbnail"),
    Source("source"),
}

interface IMSendMsgCallback {
    fun onResult(message: Message, e: Exception?)
}

interface Crypto {
    fun encrypt(text: String): String?
    fun decrypt(cipherText: String): String?
}
