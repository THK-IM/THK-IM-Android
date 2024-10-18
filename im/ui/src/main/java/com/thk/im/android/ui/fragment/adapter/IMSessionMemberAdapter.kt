package com.thk.im.android.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.viewholder.sessionmember.IMSessionMemberVH

class IMSessionMemberAdapter : RecyclerView.Adapter<IMSessionMemberVH>() {

    private val sessionMembers = mutableListOf<Pair<User, SessionMember?>>()
    var onSessionMemberClick: IMOnSessionMemberClick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IMSessionMemberVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_session_member, parent, false)
        return IMSessionMemberVH(itemView)
    }

    override fun getItemCount(): Int {
        return sessionMembers.size
    }

    override fun onBindViewHolder(holder: IMSessionMemberVH, position: Int) {
        holder.bindSessionMember(sessionMembers[position], onSessionMemberClick)
    }

    fun setData(it: MutableList<Pair<User, SessionMember?>>) {
        val oldSize = sessionMembers.size
        if (oldSize > 0) {
            sessionMembers.clear()
            notifyItemRangeRemoved(0, oldSize)
        }
        sessionMembers.addAll(it)
        notifyItemRangeInserted(0, it.size)
    }
}