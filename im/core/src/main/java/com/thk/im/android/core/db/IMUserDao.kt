package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.User

interface IMUserDao {

    fun insertUsers(vararg users: User)

    fun updateUser(vararg users: User)

    fun queryUser(id: Long): User?

    fun queryUsers(ids: Set<Long>): List<User>

    fun deleteUser(user: User)
}