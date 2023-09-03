package com.thk.im.android.core

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
    @SerializedName("thumbnailPath")
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
    @SerializedName("thumbnailPath")
    var thumbnailPath: String? = null,
) : IMCommonMsgData()

class IMAudioMsgBody(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("duration")
    var duration: Int? = null,
)

class IMImageMsgBody(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("thumbnailUrl")
    var thumbnailUrl: String? = null,
    @SerializedName("width")
    var width: Int? = null,
    @SerializedName("height")
    var height: Int? = null,
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
    @SerializedName("thumbnailUrl")
    var thumbnailUrl: String? = null,
)


