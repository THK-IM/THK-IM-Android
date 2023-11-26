package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMUserDao
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMUserDaoImp(private val roomDatabase: IMRoomDataBase) : IMUserDao {
    override fun insertUsers(vararg users: User) {
        roomDatabase.userDao().insertUsers(*users)
    }

    override fun updateUser(vararg users: User) {
        roomDatabase.userDao().updateUser(*users)
    }

    override fun queryUser(id: Long): User? {
        return roomDatabase.userDao().queryUser(id)
    }

    override fun queryUsers(ids: Set<Long>): List<User> {
        return roomDatabase.userDao().queryUsers(ids)
    }

    override fun deleteUser(user: User) {
        roomDatabase.userDao().deleteUser(user)
    }

}