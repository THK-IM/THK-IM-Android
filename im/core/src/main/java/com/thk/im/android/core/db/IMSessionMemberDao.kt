package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.SessionMember

interface IMSessionMemberDao {

    fun insertOrReplace(members: List<SessionMember>)

    fun insertOrIgnore(members: List<SessionMember>)

    fun delete(members: List<SessionMember>)

    fun findLatestSyncTimeBySessionId(sessionId: Long): Long

    fun querySessionMembers(sessionId: Long): List<SessionMember>
}