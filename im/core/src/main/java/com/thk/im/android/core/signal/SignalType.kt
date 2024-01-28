package com.thk.im.android.core.signal

enum class SignalType(val value: Int) {
    SignalNewMessage(0),
    SignalHeatBeat(1),
    SignalSyncTime(2),
    SignalConnId(3),
    SignalKickOffUser(4),
}