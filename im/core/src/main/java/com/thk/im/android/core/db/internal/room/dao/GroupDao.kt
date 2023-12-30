package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.Group

@Dao
internal interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateGroups(groups: List<Group>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreGroups(groups: List<Group>)

    @Query("select * from group_ where id = :id ")
    fun findOne(id: Long): Group?

    @Query("select * from group_ ")
    fun queryAllGroups(): List<Group>

}