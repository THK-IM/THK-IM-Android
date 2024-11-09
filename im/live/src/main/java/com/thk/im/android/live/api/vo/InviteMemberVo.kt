package com.thk.im.android.live.api.vo

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class InviteMemberReqVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("invite_u_ids")
    var inviteUIds: Set<Long>,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("msg")
    val msg: String,
): Parcelable {

}