package com.thk.im.android.core.db.dao

import androidx.room.*
import com.thk.im.android.core.db.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: User)

    @Update
    fun updateUser(vararg users: User)

    @Query("select * from user where id = :id")
    fun queryUser(id: Long): User?

    @Query("select * from user where id in (:id)")
    fun queryUsers(id: Set<Long>): List<User>

    @Delete
    fun deleteUser(user: User): Int
}