package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.LLog
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.fragment.viewholder.BaseSessionVH
import com.thk.im.android.ui.manager.IMUIManager

class SessionAdapter(private val lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<BaseSessionVH>() {

    interface OnItemClickListener {
        fun onItemClick(adapter: SessionAdapter, position: Int, session: Session)
        fun onItemLongClick(adapter: SessionAdapter, position: Int, session: Session): Boolean
    }

    var onItemClickListener: OnItemClickListener? = null

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
        holder.onViewBind(session)
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(this, position, session)
        }
        holder.itemView.setOnLongClickListener {
            if (onItemClickListener == null) {
                false
            } else {
                onItemClickListener!!.onItemLongClick(this, position, session)
            }
        }
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
            // 应该摆放的位置
            val position = findInsertPosition(session)
            if (position < pos) {
                // 应该摆放的位置靠上
                for (i in pos downTo position) {
                    if (i == position) {
                        sessionList[i] = session
                    } else {
                        sessionList[i] = sessionList[i - 1]
                    }
                }
                notifyItemRangeChanged(position, pos - position + 1)
            } else {
                // 应该摆放的位置靠下，还是放在原位
                sessionList[pos] = session
                notifyItemChanged(pos)
            }
        } else {
            val position = findInsertPosition(session)
            sessionList.add(position, session)
            notifyItemInserted(position)
        }
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