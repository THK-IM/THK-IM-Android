package com.thk.im.android.core.module

import com.thk.im.android.core.db.entity.Contact
import io.reactivex.Flowable

interface ContactModule : BaseModule {

    fun syncContacts()

    fun queryAllContact(): Flowable<List<Contact>>

    fun queryContactByUserId(entityId: Long): Flowable<Contact>

    fun updateContact(contact: Contact): Flowable<Void>

    fun queryContactsByRelation(relation: Int): Flowable<List<Contact>>

}
