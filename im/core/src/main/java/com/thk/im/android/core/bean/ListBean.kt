package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName


data class ListBean<T>(
    @SerializedName("data")
    val data: List<T>,
) {
}