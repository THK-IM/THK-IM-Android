package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.SessionMember

@Dao
interface SessionMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(members: List<SessionMember>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(members: List<SessionMember>)

    @Query("delete from session_member where session_id = :sId and user_id in (:uIds)")
    fun deleteBySIdAndUIds(sId: Long, uIds: Set<Long>)

    @Query("select max(m_time) from session_member where session_id = :sessionId")
    fun findLatestSyncTimeBySessionId(sessionId: Long): Long

    @Query("select * from session_member where session_id = :sessionId order by c_time asc")
    fun findBySessionId(sessionId: Long): List<SessionMember>

}