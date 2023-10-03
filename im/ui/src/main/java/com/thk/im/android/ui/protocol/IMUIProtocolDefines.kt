package com.thk.im.android.ui.protocol

import com.thk.im.android.ui.manager.IMFile


enum class AudioStatus(val value: Int) {
    Waiting(0),
    Ing(1),
    Exited(2),
    Finished(3)
}

interface AudioCallback {
    fun audioData(path: String, second: Int, db: Double, state: AudioStatus)
}


interface IMContentResult {

    fun onResult(result: List<IMFile>)

    fun onCancel()
}