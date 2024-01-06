package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.SessionMember

interface IMSessionMemberDao {

    fun querySessionMemberLatestTime(sessionId: Long): Long

    fun querySessionMembers(sessionId: Long): List<SessionMember>

    fun insertOrReplaceSessionMembers(members: List<SessionMember>)

    fun insertOrIgnoreSessionMembers(members: List<SessionMember>)

    fun deleteSessionMembers(members: List<SessionMember>)
}