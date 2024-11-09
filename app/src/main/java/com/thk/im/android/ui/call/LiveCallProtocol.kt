package com.thk.im.android.ui.call

import com.thk.im.android.live.Mode
import com.thk.im.android.live.room.RTCRoom

interface LiveCallProtocol {

    /**
     * 当前房间
     */
    fun room(): RTCRoom

    /**
     * 发起通话
     */
    fun requestCalling(mode: Mode, members: Set<Long>)

    /**
     * 取消calling
     */
    fun cancelCalling()

    /**
     * 接听
     */
    fun acceptCalling()

    /**
     * 拒绝接听
     */
    fun rejectCalling()

    /**
     * 挂断电话
     */
    fun hangupCalling()


}