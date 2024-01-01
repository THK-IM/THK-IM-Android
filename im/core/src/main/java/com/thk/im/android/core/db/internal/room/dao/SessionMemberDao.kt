package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.SessionMember

@Dao
interface SessionMemberDao {
    @Query("select * from session_member where session_id != :sessionId order by c_time asc")
    fun querySessionMembers(sessionId: Long): List<SessionMember>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateSessionMembers(members: List<SessionMember>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreSessionMembers(members: List<SessionMember>)
}