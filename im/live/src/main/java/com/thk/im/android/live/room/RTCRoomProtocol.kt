package com.thk.im.android.live.room

import java.nio.ByteBuffer

interface RTCRoomProtocol {

    /**
     * RTC 用户加入
     */
    fun onParticipantJoin(p: BaseParticipant)

    /**
     * RTC 用户离开
     */
    fun onParticipantLeave(p: BaseParticipant)

    /**
     * RTC 文本消息
     */
    fun onTextMsgReceived(uId: Long, text: String)

    /**
     * RTC 数据消息
     */
    fun onDataMsgReceived(data: ByteBuffer)

    /**
     * RTC 语音音量
     */
    fun onParticipantVoice(uId: Long, volume: Double)

    /**
     * RTC 连接状态
     */
    fun onConnectStatus(uId: Long, status: Int)


    /**
     *  RTC error
     */
    fun onError(function: String, ex: Exception)
}