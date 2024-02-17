package com.thk.im.android.core.db.internal.room.dao

import androidx.room.*
import com.thk.im.android.core.db.entity.Session

@Dao
internal interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(sessions: List<Session>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(sessions: List<Session>)

    @Query("delete from session where id = :sId")
    fun deleteById(sId: Long)

    /**
     * 批量删除session
     */
    @Delete
    fun delete(sessions: List<Session>): Int

    @Update
    fun update(session: Session)

    @Query("update session set top_timestamp = :top where id = :id")
    fun updateTop(id: Long, top: Long)

    @Query("update session set status = :status where id = :id")
    fun updateStatus(id: Long, status: Int)

    @Query("update session set draft = :draft where id = :id")
    fun updateDraft(id: Long, draft: String)

    @Query("update session set unread_count = :unread where id = :id")
    fun updateUnread(id: Long, unread: Int)

    @Query("update session set member_sync_time = :time where id = :id")
    fun updateMemberSyncTime(id: Long, time: Long)

    @Query("update session set msg_sync_time = :time where id = :id")
    fun updateMsgSyncTime(id: Long, time: Long)

    @Query("update session set member_count = :count where id = :id")
    fun updateMemberCount(id: Long, count: Int)

    @Query("select member_sync_time from session where id= :id ")
    fun findMemberSyncTime(id: Long): Long

    @Query("select msg_sync_time from session where id= :id ")
    fun findMsgSyncTime(id: Long): Long

    @Query("select * from session where parent_id = :parentId and id != :parentId and m_time <= :mTime order by top_timestamp desc, m_time desc limit 0, :count")
    fun findByParentId(parentId: Long, count: Int, mTime: Long): List<Session>

    @Query("select * from session where type = :type order by top_timestamp desc, m_time desc ")
    fun findAll(type: Int): List<Session>

    @Query("select * from session where entity_id = :entityId and type = :type")
    fun findByEntityId(entityId: Long, type: Int): Session?

    @Query("select * from session where id = :sId")
    fun findById(sId: Long): Session?
}