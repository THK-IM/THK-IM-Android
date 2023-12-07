package com.thk.im.android.core.api.vo

import com.google.gson.annotations.SerializedName


data class ListVo<T>(
    @SerializedName("data")
    val data: List<T>,
)