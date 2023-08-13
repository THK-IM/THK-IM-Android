package com.thk.im.android.db.dao

import androidx.room.*
import com.thk.im.android.db.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(vararg users: User)

    @Update
    fun updateUser(vararg users: User)

    @Query("select * from user where id = :id")
    fun queryUser(id: Long): User?

    @Delete
    fun deleteUser(user: User): Int
}