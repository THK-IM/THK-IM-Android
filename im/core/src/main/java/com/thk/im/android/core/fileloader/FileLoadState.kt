package com.thk.im.android.core.fileloader

enum class FileLoadState(val value: Int) {

    Wait(0),
    Init(1),
    Ing(2),
    Success(3),
    Failed(4),
}