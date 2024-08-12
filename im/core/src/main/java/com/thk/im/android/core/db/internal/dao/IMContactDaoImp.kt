package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMContactDao
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMContactDaoImp(private val roomDatabase: IMRoomDataBase) : IMContactDao {
    override fun insertOrReplace(contacts: List<Contact>) {
        roomDatabase.contactDao().insertOrReplace(contacts)
    }

    override fun insertOrIgnore(contacts: List<Contact>) {
        roomDatabase.contactDao().insertOrIgnore(contacts)
    }

    override fun findAll(): List<Contact> {
        return roomDatabase.contactDao().findAll()
    }

    override fun findByUserIds(ids: List<Long>): List<Contact> {
        return roomDatabase.contactDao().findByUserIds(ids)
    }

    override fun findByUserId(entityId: Long): Contact? {
        return roomDatabase.contactDao().findByUserId(entityId)
    }

    override fun findByRelation(relation: Int): List<Contact> {
        return roomDatabase.contactDao().findByRelation(relation)
    }
}