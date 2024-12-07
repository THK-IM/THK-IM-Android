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

    @Query("select * from session_member where session_id = :sessionId order by c_time asc")
    fun findBySessionId(sessionId: Long): List<SessionMember>

    @Query("select * from session_member where session_id = :sessionId and deleted = 0 order by c_time asc limit :offset, :count")
    fun findBySessionId(sessionId: Long, offset: Int, count: Int): List<SessionMember>

    @Query("select count(0) from session_member where session_id = :sessionId and deleted = 0")
    fun findSessionMemberCount(sessionId: Long): Int

    @Query("select * from session_member where session_id = :sessionId and user_id = :userId")
    fun findSessionMember(sessionId: Long, userId: Long): SessionMember?

    @Query("select * from session_member where session_id = :sessionId and user_id in (:userIds)")
    fun findSessionMembers(sessionId: Long, userIds: Set<Long>): List<SessionMember>

}