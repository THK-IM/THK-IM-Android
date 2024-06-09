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
            val contacts = IMCoreManager.getImDataBase().contactDao().findAll()
            it.onNext(contacts)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    open fun queryServerContactByUserId(id: Long): Flowable<Contact> {
        val contact = Contact(id, null, null, 0, null, 0, 0)
        return Flowable.just(contact)
    }

    override fun queryContactByUserId(entityId: Long): Flowable<Contact> {
        return Flowable.create<Contact?>({
            val contact = IMCoreManager.getImDataBase().contactDao().findByUserId(entityId)
            if (contact == null) {
                it.onNext(Contact(0L, null, null, 0, null, 0, 0))
            } else {
                it.onNext(contact)
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.id != entityId) {
                return@flatMap queryServerContactByUserId(entityId)
            } else {
                return@flatMap Flowable.just(it)
            }
        }
    }

    override fun updateContact(contact: Contact): Flowable<Void> {
        return Flowable.create({
            IMCoreManager.getImDataBase().contactDao().insertOrReplace(listOf(contact))
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun queryContactsByRelation(relation: Int): Flowable<List<Contact>> {
        return Flowable.create({
            val contactList =
                IMCoreManager.getImDataBase().contactDao().findByRelation(relation)
            it.onNext(contactList)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun reset() {
    }

    override fun onSignalReceived(type: Int, body: String) {
    }
}