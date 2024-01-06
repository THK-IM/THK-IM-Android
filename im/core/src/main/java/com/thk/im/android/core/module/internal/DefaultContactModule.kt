package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.module.ContactModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

open class DefaultContactModule : ContactModule {

    override fun syncContacts() {
    }

    override fun queryAllContact(): Flowable<List<Contact>> {
        return Flowable.create({
            val contacts = IMCoreManager.getImDataBase().contactDao().queryAll()
            it.onNext(contacts)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun onSignalReceived(type: Int, body: String) {
    }
}