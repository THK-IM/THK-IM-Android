package com.thk.im.android.core.db.internal.room.dao

import androidx.room.*
import com.thk.im.android.core.db.entity.User

@Dao
internal interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(users: List<User>)

    @Delete
    fun delete(user: User)

    @Query("select * from user where id = :id")
    fun findById(id: Long): User?

    @Query("select * from user where id in (:ids)")
    fun findByIds(ids: Set<Long>): List<User>
}