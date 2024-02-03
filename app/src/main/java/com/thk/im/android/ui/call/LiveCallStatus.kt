package com.thk.im.android.ui.call


enum class LiveCallStatus(val value: Int) {
    Init(1), // 初始状态
    RequestCall(1), // 请求通话
    BeRequestCall(2), // 被请求通话
    Calling(3),        // 通话中
}
