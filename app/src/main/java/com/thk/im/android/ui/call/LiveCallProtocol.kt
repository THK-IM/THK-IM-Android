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

    fun openRemoteVideo(user: User)

    fun closeRemoteVideo(user: User)

    fun openRemoteAudio(user: User)

    fun closeRemoteAudio(user: User)

    fun accept()

    fun hangup()
}