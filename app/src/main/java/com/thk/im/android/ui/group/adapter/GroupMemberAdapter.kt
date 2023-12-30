package com.thk.im.android.ui.group.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.R

class GroupMemberAdapter(
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<GroupMemberVH>() {

    val ids = mutableListOf<Long>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_group_member, parent, false)
        return GroupMemberVH(lifecycleOwner, itemView)
    }

    override fun getItemCount(): Int {
        return ids.size
    }

    override fun onBindViewHolder(holder: GroupMemberVH, position: Int) {
        val id = ids[position]
        holder.onBind(id)
    }

    fun addIds(newIds: Collection<Long>) {
        val old = this.ids.size
        this.ids.addAll(newIds)
        notifyItemRangeInserted(old, newIds.size)
    }

}