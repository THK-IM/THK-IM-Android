package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.User

interface IMUserDao {

    fun insertOrReplace(users: List<User>)

    fun insertOrIgnore(users: List<User>)

    fun delete(user: User)

    fun findById(id: Long): User?

    fun findByIds(ids: Set<Long>): List<User>
}