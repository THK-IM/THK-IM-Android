package com.thk.im.android.core.module

import com.thk.im.android.core.db.entity.Contact
import io.reactivex.Flowable

interface ContactModule : BaseModule {

    fun syncContacts()

    fun queryAllContact(): Flowable<List<Contact>>

}
