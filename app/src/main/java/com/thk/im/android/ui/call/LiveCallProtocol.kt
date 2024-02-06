package com.thk.im.android.ui.call

import com.thk.im.android.core.db.entity.User

interface LiveCallProtocol {

    /**
     * 获取本地摄像头 1 前置 2 后置
     */
    fun currentLocalCamera(): Int

    /**
     * 本地摄像头是否开启 1 开启 0 关闭
     */
    fun isCurrentCameraOpened(): Boolean

    /**
     * 切换本地摄像头
     */
    fun switchLocalCamera()

    /**
     * 打开本地摄像头
     */
    fun openLocalCamera()

    /**
     * 关闭本地摄像头
     */
    fun closeLocalCamera()

    /**
     * 打开远端视频
     */
    fun openRemoteVideo(user: User)

    /**
     * 关闭远端视频
     */
    fun closeRemoteVideo(user: User)

    /**
     * 打开远端音频
     */
    fun openRemoteAudio(user: User)

    /**
     * 关闭远端音频
     */
    fun closeRemoteAudio(user: User)

    /**
     * 接听
     */
    fun accept()

    /**
     * 挂断
     */
    fun hangup()
}