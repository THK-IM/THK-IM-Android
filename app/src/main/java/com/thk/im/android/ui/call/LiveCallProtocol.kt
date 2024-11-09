package com.thk.im.android.ui.call

interface LiveCallProtocol {

    /**
     * 扬声器是否打开
     */
    fun isSpeakerMuted(): Boolean

    /**
     * 打开/关闭扬声器
     */
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
     * 打开本地视频
     */
    fun muteLocalVideo(mute: Boolean)

    /**
     * 本地视频是否打开
     */
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
    fun isRemoteAudioMuted(uId: Long): Boolean

    /**
     * 打开/关闭远端视频
     */
    fun muteRemoteVideo(uId: Long, mute: Boolean)

    /**
     * 远端视频是否关闭
     */
    fun isRemoteVideoMuted(uId: Long): Boolean

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