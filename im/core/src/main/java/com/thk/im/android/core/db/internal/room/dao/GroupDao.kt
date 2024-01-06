package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.Group

@Dao
internal interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(groups: List<Group>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(groups: List<Group>)

    @Query("delete from group_ where id in (:ids)")
    fun deleteByIds(ids: Set<Long>)

    @Query("select * from group_ where id = :id ")
    fun findById(id: Long): Group?

    @Query("select * from group_ ")
    fun findAll(): List<Group>

}