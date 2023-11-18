package com.thk.im.android.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.Group

@Dao
interface GroupDao {

    /**
     * 创建一个群
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(vararg groups: Group)

    /**
     * 查找群
     */
    @Query("select * from group_ where id = :gid")
    fun findGroup(gid: Long): Group?

    /**
     * 删除一个群
     */
    @Delete
    fun deleteGroup(group: Group): Int

    /**
     * 删除一个群
     */
    @Query("delete from group_ where id = :gid")
    fun deleteGroupById(gid: Long): Int
}