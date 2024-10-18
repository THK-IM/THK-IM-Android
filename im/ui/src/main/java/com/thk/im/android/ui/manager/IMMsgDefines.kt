package com.thk.im.android.ui.manager

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Message

@Keep
class IMAudioMsgData(
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
)

@Keep
class IMImageMsgData(
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("thumbnail_path")
    var thumbnailPath: String? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
)

@Keep
class IMVideoMsgData(
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("thumbnail_path")
    var thumbnailPath: String? = null,
)

@Keep
class IMAudioMsgBody(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("name")
    var name: String? = null,
)

@Keep
class IMImageMsgBody(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("thumbnail_url")
    var thumbnailUrl: String? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
    @SerializedName("name")
    var name: String? = null,
)

@Keep
class IMVideoMsgBody(
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("thumbnail_url")
    var thumbnailUrl: String? = null,
    @SerializedName("name")
    var name: String? = null,
)

@Keep
class IMRevokeMsgData(
    @SerializedName("nick")
    var nick: String,
    @SerializedName("type")
    var type: Int? = null,
    @SerializedName("content")
    var content: String? = null,
    @SerializedName("data")
    var data: String? = null
)

@Keep
data class IMRecordMsgBody(
    @SerializedName("title")
    var title: String,
    @SerializedName("messages")
    var messages: List<Message>,
    @SerializedName("content")
    var content: String
)

@Keep
data class IMReeditMsgData(
    @SerializedName("session_id")
    val sessionId: Long,
    @SerializedName("origin_id")
    val originId: Long,
    @SerializedName("edit")
    val edit: String
)


