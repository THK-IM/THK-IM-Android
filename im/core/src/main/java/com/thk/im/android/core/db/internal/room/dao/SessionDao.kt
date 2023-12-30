package com.thk.im.android.core.db.internal.room.dao

import androidx.room.*
import com.thk.im.android.core.db.entity.Session

@Dao
internal interface SessionDao {

    @Query("select * from session where parent_id = :parentId and id != :parentId and m_time <= :mTime order by top_timestamp desc, m_time desc limit 0, :count")
    fun querySessions(parentId: Long, count: Int, mTime: Long): List<Session>

    @Query("select * from session order by m_time desc")
    fun querySessionsByMTime(): List<Session>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateSessions(sessions: List<Session>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreSessions(sessions: List<Session>)

    @Query("select * from session where entity_id = :entityId and type = :type")
    fun findSessionByEntity(entityId: Long, type: Int): Session?

    @Query("select * from session where id = :sid")
    fun findSession(sid: Long): Session?

    @Update
    fun updateSession(session: Session)

    @Query("delete from session where id = :sid")
    fun deleteSessionById(sid: Long)

    /**
     * 批量删除session
     */
    @Delete
    fun deleteSessions(vararg session: Session): Int

    @Query("update session set top_timestamp = :top where id = :sid")
    fun updateTop(sid: Long, top: Long)

    @Query("update session set status = :status where id = :sid")
    fun updateStatus(sid: Long, status: Int)

    @Query("update session set draft = :draft where id = :sid")
    fun updateDraft(sid: Long, draft: String)

    @Query("update session set unread_count = :unread where id = :sid")
    fun updateUnread(sid: Long, unread: Int)
}