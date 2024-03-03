package com.thk.im.android.ui.manager

import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Message


class IMAudioMsgData(
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("played")
    var played: Boolean = false,
)

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

class IMAudioMsgBody(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("name")
    var name: String? = null,
)

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


data class IMRecordMsgBody(
    @SerializedName("title")
    var title: String,
    @SerializedName("messages")
    var messages: List<Message>,
    @SerializedName("content")
    var content: String
)

data class IMReeditMsgData(
    @SerializedName("session_id")
    val sessionId: Long,
    @SerializedName("origin_id")
    val originId: Long,
    @SerializedName("edit")
    val edit: String
)


