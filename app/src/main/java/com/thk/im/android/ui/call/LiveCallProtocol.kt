package com.thk.im.android.ui.call

interface LiveCallProtocol {

    fun isSpeakerOn(): Boolean

    fun muteSpeaker(mute: Boolean)

    /**
     * 获取本地摄像头: 0 未知, 1 后置, 2 前置
     */
    fun currentLocalCamera(): Int

    /**
     * 切换本地摄像头
     */
    fun switchLocalCamera()

    /**
     * 打开本地摄像头
     */
    fun muteLocalVideo(mute: Boolean)

    fun isLocalVideoMuted(): Boolean

    /**
     * 打开/关闭本地音频
     */
    fun muteLocalAudio(mute: Boolean)

    /**
     * 本地音频是否关闭
     */
    fun isLocalAudioMuted(): Boolean

    /**
     * 打开/关闭远端音频
     */
    fun muteRemoteAudio(uId: Long, mute: Boolean)

    /**
     * 远端音频是否关闭
     */
    fun isRemoteAudioMuted(): Boolean

    /**
     * 打开/关闭远端视频
     */
    fun muteRemoteVideo(uId: Long, mute: Boolean)

    /**
     * 远端视频是否关闭
     */
    fun isRemoteVideoMuted(uId: Long): Boolean

    /**
     * 接听
     */
    fun accept()

    /**
     * 挂断
     */
    fun hangup()
}