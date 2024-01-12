package com.thk.im.android.ui.main.fragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.R
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.manager.IMUIManager
import io.reactivex.Flowable


class ContactAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val mode: Int
) :
    RecyclerView.Adapter<ContactVH>(), ContactItemOperator {

    private val contactList = mutableListOf<Contact>()
    val selectedIds = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_contact, parent, false)
        return ContactVH(lifecycleOwner, itemView)
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: ContactVH, position: Int) {
        val contact = contactList[position]
        holder.onBind(contact, selectedIds.contains(contact.id), this)
    }

    fun setContactList(data: List<Contact>) {
        val oldSize = contactList.size
        contactList.clear()
        notifyItemRangeRemoved(0, oldSize)
        contactList.addAll(data)
        notifyItemRangeInserted(0, contactList.size)
    }

    private fun openSession(contact: Contact) {
        val subscriber: BaseSubscriber<Session> = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session?) {
                t?.let { session ->
                    IMUIManager.pageRouter?.openSession(this@ContactAdapter.context, session)
                }
            }
        }
        IMCoreManager.messageModule.getSession(contact.id, SessionType.Single.value)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    private fun createSession(contact: Contact) {
        val req = ContactSessionCreateVo(IMCoreManager.uId, contact.id)


        val subscriber: BaseSubscriber<Session> = object : BaseSubscriber<Session>() {
            override fun onNext(t: Session?) {
                t?.let { session ->
                    IMUIManager.pageRouter?.openSession(this@ContactAdapter.context, session)
                }
            }
        }
        DataRepository.contactApi.createContactSession(req)
            .flatMap {
                return@flatMap Flowable.just(it.toSession())
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
    }

    override fun onItemClick(contact: Contact) {
        if (mode == 0) {
            val sessionId = contact.sessionId
            if (sessionId != null) {
                openSession(contact)
            } else {
                createSession(contact)
            }
        } else {
            val id = contact.id
            if (selectedIds.contains(id)) {
                selectedIds.remove(id)
            } else {
                selectedIds.add(id)
            }
            for ((position, contact) in contactList.withIndex()) {
                if (contact.id == id) {
                    notifyItemChanged(position)
                }
            }

        }
    }

}