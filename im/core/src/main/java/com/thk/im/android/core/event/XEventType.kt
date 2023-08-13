package com.thk.im.android.core.event

enum class XEventType(val value: String) {
    MsgNew("MsgNew"),
    MsgUpdate("MsgUpdated"),
    MsgDeleted("MsgDeleted"),
    MsgSendFailed("MsgSendFailed"),

    SessionNew("SessionNew"),
    SessionUpdate("SessionUpdate"),
    SessionDeleted("SessionDeleted"),

    Connecting("Connecting"),
    UnConnected("UnConnected"),
    Connected("Connected"),
}