package com.thk.im.android.live.player

import android.content.res.AssetFileDescriptor
import java.io.FileDescriptor

interface IRTCMediaPlayerCallback {

    /**
     * 播放时间更新
     */
    fun onPlayTimeUpdate(currentTime: Long, totalTime: Long)

    /**
     * 播放结束
     */
    fun onPlayEnded(path: String)

    /**
     * 播放错误
     */
    fun onPlayerError(ex: Throwable)

}


interface IRTCMediaPlayer {

    /**
     * 设置播放回调
     */
    fun setPlayCallback(callback: IRTCMediaPlayerCallback?)

    /**
     * 更新RTC参数
     */
    fun updateRTCParams(numChannels: Int, sampleRateHz: Int)


    /**
     * 获取PCM数据
     */
    fun fetchRTCPCM(len: Int): ByteArray?

    /**
     * 播放媒体
     */
    fun setMediaItem(path: String)


    /**
     * 播放媒体
     */
    fun setMediaItem(fd: FileDescriptor)


    /**
     * 播放媒体
     */
    fun setMediaItem(afd: AssetFileDescriptor)


    /**
     * 暂停
     */
    fun pause()

    /**
     * 播放
     */
    fun play()

    /**
     * 跳转到时间， 单位ms
     */
    fun seekTo(time: Long)


    /**
     * 释放所有资源
     */
    fun release()

}