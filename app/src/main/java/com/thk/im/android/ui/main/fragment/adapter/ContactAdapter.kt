package com.thk.im.android.ui.main.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.R
import com.thk.im.android.core.db.entity.Contact


class ContactAdapter(
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<ContactVH>() {

    private val contactList = mutableListOf<Contact>()
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
        holder.onBind(contact)
    }

    fun setContactList(data: List<Contact>) {
        val oldSize = contactList.size
        contactList.clear()
        notifyItemRangeRemoved(0, oldSize)
        contactList.addAll(data)
        notifyItemRangeInserted(0, contactList.size)
    }

}