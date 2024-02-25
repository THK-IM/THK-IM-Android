package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.msg.viewholder.BaseSessionVH
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator
import kotlin.math.abs

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
            onNewSession(s)
        }
    }

    fun onNewSession(session: Session) {
        onSessionUpdate(session)
    }

    fun onSessionUpdate(session: Session) {
        // 当前位置
        val oldPos = findPosition(session)
        if (oldPos >= 0 && oldPos < sessionList.size) {
            sessionList.removeAt(oldPos)
            val newPos = findInsertPosition(session)
            sessionList.add(newPos, session)
            val lessPos = if (newPos > oldPos) {
                oldPos
            } else {
                newPos
            }
            notifyItemRangeChanged(lessPos, abs(newPos - oldPos) + 1)
//            if (newPos == oldPos) {
//                notifyItemChanged(newPos)
//            } else {
//                notifyItemRemoved(oldPos)
//                notifyItemInserted(newPos)
//            }
        } else {
            val newPos = findInsertPosition(session)
            sessionList.add(newPos, session)
            notifyItemInserted(newPos)
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
            if (session.topTimestamp > s.topTimestamp) {
                return pos
            } else if (session.topTimestamp == s.topTimestamp) {
                if (session.mTime >= s.mTime) {
                    return pos
                }
            }
        }
        return sessionList.size
    }

    fun onSessionRemove(session: Session) {
        val pos = findPosition(session)
        if (pos >= 0) {
            sessionList.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    fun getSessionList(): List<Session> {
        return sessionList
    }
}