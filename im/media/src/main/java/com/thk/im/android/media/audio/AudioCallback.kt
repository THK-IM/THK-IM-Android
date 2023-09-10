package com.thk.im.android.media.audio

enum class AudioStatus(val value: Int) {
    Waiting(0),
    Ing(1),
    Exited(2),
    Finished(3)
}

interface AudioCallback {
    fun notify(path: String, second: Int, db: Double, state: AudioStatus)
}