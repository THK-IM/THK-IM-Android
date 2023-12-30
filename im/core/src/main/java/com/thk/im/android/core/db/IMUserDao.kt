package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.User

interface IMUserDao {

    fun insertOrReplaceUsers(users: List<User>)

    fun insertOrIgnoreUsers(users: List<User>)

    fun queryUser(id: Long): User?

    fun queryUsers(ids: Set<Long>): List<User>

    fun deleteUser(user: User)
}