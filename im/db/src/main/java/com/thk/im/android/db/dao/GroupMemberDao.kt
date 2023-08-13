package com.thk.im.android.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.db.entity.GroupMember

@Dao
interface GroupMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupMembers(vararg groupMember: GroupMember)

    @Query("select * from group_member where gid=:gid")
    fun queryGroupMembers(gid: Long): List<GroupMember>

    /**
     * 分页查询群组成员
     */
    @Query("select * from group_member order by m_time desc limit :offset, :size")
    fun queryGroupMemberByMTime(offset: Int, size: Int): List<GroupMember>

    /**
     * 删除一个群成员
     */
    @Delete
    fun deleteGroupMember(vararg groupMember: GroupMember)

    /**
     * 删除一个群成员
     */
    @Query("delete from group_member where gid = :gid and uid=:uid")
    fun deleteGroupMemberById(gid: Long, uid: Long)

    /**
     * 查询单个群成员
     */
    @Query("select * from group_member where uid = :uid")
    fun queryGroupMember(uid: Long): GroupMember?

    @Query("select max(c_time) from group_member")
    fun findLatestGroupMemberCTime(): Long
}