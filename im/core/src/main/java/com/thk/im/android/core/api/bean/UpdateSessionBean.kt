package com.thk.im.android.core.api.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UpdateSessionBean(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("s_id")
    var sId: Long,
    @SerializedName("top")
    var top: Long?,
    @SerializedName("status")
    var status: Int?,
) : Parcelable