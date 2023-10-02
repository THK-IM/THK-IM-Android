package com.thk.im.android.ui.manager

import com.google.gson.annotations.SerializedName


open class IMCommonMsgData(
    @SerializedName("reply_msg_ids")
    var replyMsgIds: MutableSet<Long>? = null,
    @SerializedName("read_u_ids")
    var readUIds: MutableSet<Long>? = null,
)

class IMAudioMsgData(
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
    @SerializedName("played")
    var played: Boolean = false,
) : IMCommonMsgData()

class IMImageMsgData(
    @SerializedName("path")
    var path: String? = null,
    @SerializedName("thumbnail_path")
    var thumbnailPath: String? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
) : IMCommonMsgData()

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
) : IMCommonMsgData()

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


