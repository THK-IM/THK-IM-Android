package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Contact

interface IMContactDao {


    fun insertOrUpdateContacts(contacts: List<Contact>)

    fun insertOrIgnoreContacts(contacts: List<Contact>)

    fun queryAllContacts() : List<Contact>
}