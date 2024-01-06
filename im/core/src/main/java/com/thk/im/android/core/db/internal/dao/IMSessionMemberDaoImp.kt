package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionMemberDao
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionMemberDaoImp(private val roomDatabase: IMRoomDataBase) :
    IMSessionMemberDao {

    override fun insertOrReplace(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrReplace(members)
    }

    override fun insertOrIgnore(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrIgnore(members)
    }

    override fun delete(members: List<SessionMember>) {
        if (members.isNotEmpty()) {
            val sId = members.last().sessionId
            val uIds = mutableSetOf<Long>()
            for (m in members) {
                uIds.add(m.userId)
            }
            return roomDatabase.sessionMemberDao().deleteBySIdAndUIds(sId, uIds)
        }
    }

    override fun findBySessionId(sessionId: Long): List<SessionMember> {
        return roomDatabase.sessionMemberDao().findBySessionId(sessionId)
    }
}