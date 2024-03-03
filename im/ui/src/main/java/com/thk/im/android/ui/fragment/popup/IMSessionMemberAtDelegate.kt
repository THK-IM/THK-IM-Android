package com.thk.im.android.ui.fragment.popup

import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User

interface IMSessionMemberAtDelegate {

    fun onSessionMemberAt(sessionMember: SessionMember, user: User)

}