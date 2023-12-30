package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMGroupDao
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMGroupDaoImp(private val roomDatabase: IMRoomDataBase) : IMGroupDao {
    override fun insertOrUpdateGroups(groups: List<Group>) {
        roomDatabase.groupDao().insertOrUpdateGroups(groups)
    }

    override fun insertOrIgnoreGroups(groups: List<Group>) {
        roomDatabase.groupDao().insertOrIgnoreGroups(groups)
    }

    override fun queryAllGroups(): List<Group> {
        return roomDatabase.groupDao().queryAllGroups()
    }

    override fun findOne(id: Long): Group? {
        return roomDatabase.groupDao().findOne(id)
    }

}