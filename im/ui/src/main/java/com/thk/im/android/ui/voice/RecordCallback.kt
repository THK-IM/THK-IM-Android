package com.thk.im.android.ui.voice

interface RecordCallback {
    /**
     * 声音回调
     */
    fun onVolumeChange(recorder: IRecorder, volume: Int)

    /**
     * 录制时长回调
     */
    fun onProcess(recorder: IRecorder, duration: Int)

    /**
     * 录制完成回调
     */
    fun onRecordFinish(recorder: IRecorder, duration: Int)
}