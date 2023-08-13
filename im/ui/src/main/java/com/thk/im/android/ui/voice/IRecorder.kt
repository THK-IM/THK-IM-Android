package com.thk.im.android.ui.voice

interface IRecorder {

    /**
     * 开始录制
     */
    fun startRecord()

    /**
     * 停止录制
     */
    fun stopRecord()

    /**
     * 释放资源
     */
    fun release()

    /**
     * 设置录制回调，可以获取录制时长
     */
    fun setRecordCallback(listener: RecordCallback)

    /**
     * 获取音频文件保存的路径
     */
    fun getFilePath(): String

    fun getFileName(): String

    /**
     * 设置最大录音时长
     */
    fun setMaxDuration(maxDuration: Int)
}