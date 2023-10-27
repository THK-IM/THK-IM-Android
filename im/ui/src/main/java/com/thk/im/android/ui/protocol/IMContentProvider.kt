package com.thk.im.android.ui.protocol

import android.app.Activity
import android.view.View
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.ui.manager.MediaItem

interface IMContentProvider {

    fun preview(activity: Activity, items: ArrayList<MediaItem>, view: View, position: Int)

    fun openCamera(
        activity: Activity,
        formats: List<IMFileFormat>,
        imContentResult: IMContentResult
    )

    fun pick(activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult)

    fun startRecordAudio(path: String, duration: Int, audioCallback: AudioCallback): Boolean

    fun stopRecordAudio()

    fun isRecordingAudio(): Boolean

    fun startPlayAudio(path: String, audioCallback: AudioCallback): Boolean

    fun stopPlayAudio()

    fun isPlayingAudio(): Boolean
}