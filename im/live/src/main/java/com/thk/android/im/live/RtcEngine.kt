package com.thk.android.im.live

import com.thk.android.im.live.room.Role

interface RtcEngine {

    /**
     * 加入频道
     */
    fun joinChannel(channel: String, role: Role)

    /**
     * 离开频道
     */
    fun leaveChannel()

    /**
     * 本地静音
     */
    fun muteLocal()

    /**
     * 远端静音
     */
    fun muteRemote()
}