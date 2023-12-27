package com.thk.im.android.core.api.vo

import com.google.gson.annotations.SerializedName

class PageListVo<T>(
    @SerializedName("total")
    val total: Long,
    @SerializedName("data")
    val data: List<T>,
)