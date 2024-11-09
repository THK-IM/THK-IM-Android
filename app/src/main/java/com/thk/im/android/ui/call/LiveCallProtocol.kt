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

    /**
     * 对方接听
     */
    fun onRemoteAcceptedCalling(roomId: String, uId: Long)

    /**
     * 对方拒绝接听
     */
    fun onRemoteRejectedCalling(roomId: String, uId: Long, msg: String)

    /**
     * 对方挂断电话
     */
    fun onRemoteHangupCalling(roomId: String, uId: Long, msg: String)

    /**
     * 被踢下
     */
    fun onMemberKickedOff(roomId: String, uIds: Set<Long>)

    /**
     * 房间通话结束
     */
    fun onCallEnded(roomId: String)

}