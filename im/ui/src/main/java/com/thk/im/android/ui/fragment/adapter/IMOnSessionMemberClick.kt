package com.thk.im.android.ui.fragment.adapter

import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User

interface IMOnSessionMemberClick {
    fun onSessionMemberClick(user: User, sessionMember: SessionMember?)
}