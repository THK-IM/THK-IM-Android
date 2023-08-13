package com.thk.im.android.core.signal

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.module.internal.CommonSubType

data class Signal(
    @SerializedName("type")
    val type: Int,
    @SerializedName("sub_type")
    val subType: Int,
    @SerializedName("body")
    val Body: String
) {
    companion object {
        val heatBeat: String =
            Gson().toJson(Signal(SignalType.Common.value, CommonSubType.PING.value, "ping"))
    }
}



