package com.thk.im.android.core.signal

enum class SignalType(val value: Int) {
    SignalNewMessage(0),
    SignalPing(1),
    SignalPong(2),
    SignalSyncTime(3),
    SignalConnId(4),
    SignalKickOffUser(5),
}