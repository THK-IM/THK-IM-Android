package com.thk.im.android.core.api.vo

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UpdateUserSession(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("s_id")
    var sId: Long,
    @SerializedName("top")
    var top: Long?,
    @SerializedName("status")
    var status: Int?,
    @SerializedName("parent_id")
    var parentId: Long?
) : Parcelable