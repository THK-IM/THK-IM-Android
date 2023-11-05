package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.LLog
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.fragment.viewholder.BaseSessionVH
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator

class SessionAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val sessionOperator: IMSessionVHOperator
) :
    RecyclerView.Adapter<BaseSessionVH>() {

    private val sessionList = mutableListOf<Session>()

    override fun getItemViewType(position: Int): Int {
        val session = sessionList[position]
        val provider = IMUIManager.getSessionIVProvider(session.type)
        return provider.viewType(session)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSessionVH {
        val provider = IMUIManager.getSessionIVProvider(viewType)
        return provider.viewHolder(lifecycleOwner, viewType, parent)
    }

    override fun onBindViewHolder(holder: BaseSessionVH, position: Int) {
        val session = sessionList[position]
        holder.onViewBind(session, sessionOperator)
    }

    override fun onViewRecycled(holder: BaseSessionVH) {
        super.onViewRecycled(holder)
        LLog.v("onViewRecycled")
        holder.onViewDetached()
    }

    override fun getItemCount(): Int {
        return sessionList.size
    }

    fun setData(sessions: List<Session>) {
        synchronized(this) {
            val oldSize = sessionList.size
            sessionList.clear()
            notifyItemRangeRemoved(0, oldSize)
            sessionList.addAll(sessions)
            notifyItemRangeInserted(0, sessionList.size)
        }
    }

    fun addData(sessions: List<Session>) {
        for (s in sessions) {
            insertNew(s)
        }
    }

    fun insertNew(session: Session) {
        update(session)
    }

    fun update(session: Session) {
        // 当前位置
        val pos = findPosition(session)
        if (pos >= 0 && pos < sessionList.size) {
            sessionList.removeAt(pos)
            notifyItemRemoved(pos)
        }
        val position = findInsertPosition(session)
        sessionList.add(position, session)
        notifyItemInserted(position)
    }

    private fun findPosition(session: Session): Int {
        for ((pos, s) in sessionList.withIndex()) {
            if (session.id == s.id) {
                return pos
            }
        }
        return -1
    }


    private fun findInsertPosition(session: Session): Int {
        for ((pos, s) in sessionList.withIndex()) {
            if (session.topTime > s.topTime) {
                return pos
            } else if (session.topTime == s.topTime) {
                if (session.mTime >= s.mTime) {
                    return pos
                }
            }
        }
        return sessionList.size
    }

    fun delete(session: Session) {
        val pos = findPosition(session)
        if (pos >= 0) {
            sessionList.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}