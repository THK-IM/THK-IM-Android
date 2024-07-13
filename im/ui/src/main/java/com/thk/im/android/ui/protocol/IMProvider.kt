package com.thk.im.android.ui.protocol

import android.app.Activity
import com.thk.im.android.core.IMFileFormat

interface IMProvider {

    fun openCamera(
        activity: Activity,
        formats: List<IMFileFormat>,
        imContentResult: IMContentResult
    )

    fun pick(activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult)

    fun startRecordAudio(
        path: String,
        duration: Int,
        audioCallback: AudioCallback
    ): Boolean

    fun stopRecordAudio()

    fun isRecordingAudio(): Boolean

    fun startPlayAudio(path: String, audioCallback: AudioCallback): Boolean

    fun stopPlayAudio()

    fun currentPlayingPath(): String?

    fun isPlayingAudio(): Boolean
}