package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMUserDao
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMUserDaoImp(private val roomDatabase: IMRoomDataBase) : IMUserDao {

    override fun insertOrReplace(users: List<User>) {
        roomDatabase.userDao().insertOrReplace(users)
    }

    override fun insertOrIgnore(users: List<User>) {
        roomDatabase.userDao().insertOrIgnore(users)
    }

    override fun delete(user: User) {
        roomDatabase.userDao().delete(user)
    }

    override fun findById(id: Long): User? {
        return roomDatabase.userDao().findById(id)
    }

    override fun findByIds(ids: Set<Long>): List<User> {
        return roomDatabase.userDao().findByIds(ids)
    }

}