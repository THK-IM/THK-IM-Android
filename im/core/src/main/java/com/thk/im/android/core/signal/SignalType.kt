package com.thk.im.android.core.signal

enum class SignalType(val value: Int) {
    Common(0),
    User(1),
    Contactor(2),
    Group(3),
    Message(4),
    SelfDefine(5),
}