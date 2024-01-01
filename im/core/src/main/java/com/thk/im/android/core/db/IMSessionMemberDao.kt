package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.SessionMember

interface IMSessionMemberDao {

    fun querySessionMembers(sessionId: Long): List<SessionMember>

    fun insertOrUpdateSessionMembers(members: List<SessionMember>)

    fun insertOrIgnoreSessionMembers(members: List<SessionMember>)
}