package com.thk.im.android.ui.main.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.R
import com.thk.im.android.core.db.entity.Group

class GroupAdapter(
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<GroupVH>() {

    private val groupList = mutableListOf<Group>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_group, parent, false)
        return GroupVH(lifecycleOwner, itemView)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: GroupVH, position: Int) {
        val group = groupList[position]
        holder.onBind(group)
    }

    fun setGroupList(data: List<Group>) {
        val oldSize = groupList.size
        groupList.clear()
        notifyItemRangeRemoved(0, oldSize)
        groupList.addAll(data)
        notifyItemRangeInserted(0, groupList.size)
    }

}