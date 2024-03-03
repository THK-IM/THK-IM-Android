package com.thk.im.android.ui.protocol.internal

import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User

interface IMSessionMemberAtDelegate {

    fun onSessionMemberAt(sessionMember: SessionMember, user: User)

}