package com.thk.im.android.core.signal

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Signal(
    @SerializedName("type")
    val type: Int,
    @SerializedName("body")
    val body: String
) {
    companion object {
        val ping: String =
            Gson().toJson(Signal(SignalType.SignalHeatBeat.value, "ping"))
    }
}



