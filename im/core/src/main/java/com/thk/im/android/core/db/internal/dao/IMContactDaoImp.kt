package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMContactDao
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMContactDaoImp(private val roomDatabase: IMRoomDataBase) : IMContactDao {
    override fun insertOrUpdateContacts(contacts: List<Contact>) {
        roomDatabase.contactDao().insertOrUpdateContacts(contacts)
    }

    override fun insertOrIgnoreContacts(contacts: List<Contact>) {
        roomDatabase.contactDao().insertOrIgnoreContacts(contacts)
    }

    override fun queryAllContacts(): List<Contact> {
        return roomDatabase.contactDao().queryAllContacts()
    }
}