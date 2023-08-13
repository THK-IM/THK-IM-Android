package com.thk.android.im.live.room

import com.thk.android.im.live.participant.BaseParticipant

interface RoomObserver {

    fun join(p: BaseParticipant)

    fun leave(p: BaseParticipant)
}