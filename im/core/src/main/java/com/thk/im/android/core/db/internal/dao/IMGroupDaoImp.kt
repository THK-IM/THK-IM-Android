package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMGroupDao
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMGroupDaoImp(private val roomDatabase: IMRoomDataBase) : IMGroupDao {
    override fun insertOrReplace(groups: List<Group>) {
        roomDatabase.groupDao().insertOrReplace(groups)
    }

    override fun insertOrIgnore(groups: List<Group>) {
        roomDatabase.groupDao().insertOrIgnore(groups)
    }

    override fun deleteByIds(ids: Set<Long>) {
        return roomDatabase.groupDao().deleteByIds(ids)
    }

    override fun queryAll(): List<Group> {
        return roomDatabase.groupDao().queryAll()
    }

    override fun findById(id: Long): Group? {
        return roomDatabase.groupDao().findById(id)
    }

}