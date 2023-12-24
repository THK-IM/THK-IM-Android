package com.thk.im.android.api.contact.vo

import com.google.gson.annotations.SerializedName

data class CreateSessionResp(
    @SerializedName("s_id")
    var id: Long = 0,
    @SerializedName("parent_id")
    var parentId: Long = 0,
    @SerializedName("entity_id")
    var entityId: Long = 0,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("remark")
    var remark: String = "",
    @SerializedName("top")
    var top: Long = 0,
    @SerializedName("role")
    var role: Int = 0,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("mute")
    var mute: Int = 0,
    @SerializedName("ext_data")
    var extData: String? = null,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    var mTime: Long = 0,
    @SerializedName("is_new")
    var isNew: Int = 0,
)