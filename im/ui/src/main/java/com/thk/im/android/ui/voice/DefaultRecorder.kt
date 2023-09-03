package com.thk.im.android.ui.voice

import android.content.Context
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.media.audio.OpusRecorder

class DefaultRecorder(val context: Context, val sid: Long) : IRecorder {

    private val MAX_SOUND_RECORD_TIME: Int = 120 // 单位秒


    private var currentFileName: String = ""

    /**
     * 最大录制时长
     */
    private var maxDuration: Int = MAX_SOUND_RECORD_TIME


    private var recorder: OpusRecorder = OpusRecorder.getInstance()

    private var recordCallback: RecordCallback? = null


    override fun startRecord() {
        recorder.startRecording(getFilePath())
        recorder.setRecordCallback(object : OpusRecorder.RecordCallback {
            override fun updateProgress(duration: Float) {
                val stop = duration >= maxDuration
                if (stop) {
                    stopRecord()
                } else {
                    recordCallback?.onProcess(this@DefaultRecorder, duration.toInt())
                }
            }

            override fun updateVolume(voiceValue: Int) {
                recordCallback?.onVolumeChange(this@DefaultRecorder, voiceValue)
            }

            override fun onRecordFinish(duration: Float) {
                recordCallback?.onRecordFinish(this@DefaultRecorder, duration.toInt())
            }

        })
    }

    override fun stopRecord() {
        recorder.stopRecording()
    }

    override fun release() {
        recorder.release()
    }

    override fun setRecordCallback(listener: RecordCallback) {
        this.recordCallback = listener
    }

    override fun getFilePath(): String {
        return IMCoreManager.getStorageModule()
            .allocSessionFilePath(
                sid,
                System.currentTimeMillis().toString() + ".spx",
                "voice"
            )
    }

    override fun getFileName(): String {
        return currentFileName
    }

    override fun setMaxDuration(maxDuration: Int) {
        this@DefaultRecorder.maxDuration = maxDuration
    }
}
