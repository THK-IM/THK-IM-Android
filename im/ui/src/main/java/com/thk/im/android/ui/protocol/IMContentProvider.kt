package com.thk.im.android.ui.protocol

import android.app.Activity
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.ui.manager.IMFile

interface IMContentProvider {

    fun openCamera(activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult)

    fun pick(activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult)

    fun startRecordAudio(path: String, duration: Int, audioCallback: AudioCallback): Boolean

    fun stopRecordAudio()

    fun isRecordingAudio(): Boolean

    fun startPlayAudio(path: String, audioCallback: AudioCallback): Boolean

    fun stopPlayAudio()

    fun isPlayingAudio(): Boolean
}

interface IMContentResult {

    fun onResult(result: List<IMFile>)

    fun onCancel()
}

enum class AudioStatus(val value: Int) {
    Waiting(0),
    Ing(1),
    Exited(2),
    Finished(3)
}

interface AudioCallback {
    fun audioData(path: String, second: Int, db: Double, state: AudioStatus)
}