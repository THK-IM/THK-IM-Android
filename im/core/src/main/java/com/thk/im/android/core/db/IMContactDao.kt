package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Contact

interface IMContactDao {

    fun insertOrReplace(contacts: List<Contact>)

    fun insertOrIgnore(contacts: List<Contact>)

    fun findAll(): List<Contact>

    fun findByUserId(entityId: Long): Contact?

    fun findByRelation(relation: Int): List<Contact>
}