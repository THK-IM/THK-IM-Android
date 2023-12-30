package com.thk.im.android.core.db.internal.room.dao

import androidx.room.*
import com.thk.im.android.core.db.entity.User

@Dao
internal interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreUsers(users: List<User>)

    @Query("select * from user where id = :id")
    fun queryUser(id: Long): User?

    @Query("select * from user where id in (:ids)")
    fun queryUsers(ids: Set<Long>): List<User>

    @Delete
    fun deleteUser(user: User)
}