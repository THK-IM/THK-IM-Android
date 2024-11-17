package com.thk.im.android.ui.call

import com.thk.im.android.live.room.RTCRoom

interface LiveCallProtocol {

    /**
     * 当前房间
     */
    fun room(): RTCRoom

    /**
     * 发起通话
     */
    fun startRequestCalling()

    /**
     * 取消calling
     */
    fun cancelRequestCalling()

    /**
     * 挂断电话
     */
    fun hangupCalling()

    /**
     * 对方接听
     */
    fun onRemoteAcceptedCallingBySignal(roomId: String, uId: Long)

    /**
     * 对方拒绝接听
     */
    fun onRemoteRejectedCallingBySignal(roomId: String, uId: Long, msg: String)

    /**
     * 对方挂断电话
     */
    fun onRemoteHangupCallingBySignal(roomId: String, uId: Long, msg: String)

    /**
     * 被踢下
     */
    fun onMemberKickedOffBySignal(roomId: String, uIds: Set<Long>, msg: String)

    /**
     * 房间通话结束
     */
    fun onCallEndedBySignal(roomId: String)

}