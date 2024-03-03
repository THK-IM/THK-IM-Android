package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.viewholder.msg.IMBaseMsgVH
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import kotlin.math.abs

class IMMessageAdapter(
    private val session: Session,
    private val lifecycleOwner: LifecycleOwner,
    private val msgVHOperator: IMMsgVHOperator
) :
    RecyclerView.Adapter<IMBaseMsgVH>() {

    private var lastMessageTime: Long = 0L
    private val timeLineInterval = 5 * 60 * 1000

    private val messageList = mutableListOf<Message>()
    private var selectedMessages = hashSetOf<Message>()
    private var isSelectMode = false

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val provider = IMUIManager.getMsgIVProviderByMsgType(message.type)
        return provider.viewType(message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IMBaseMsgVH {
        val provider = IMUIManager.getMsgIVProviderByViewType(viewType)
        return provider.viewHolder(lifecycleOwner, viewType, parent)
    }

    override fun onBindViewHolder(holder: IMBaseMsgVH, position: Int) {
        holder.onViewBind(position, messageList, session, msgVHOperator)
    }

    override fun onViewRecycled(holder: IMBaseMsgVH) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    override fun onViewAttachedToWindow(holder: IMBaseMsgVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
    }

    override fun onViewDetachedFromWindow(holder: IMBaseMsgVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetached()
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    private fun newTimelineMessage(cTime: Long): Message {
        return Message(
            0L, 0L, 0L, 0L, MsgType.TimeLine.value, cTime.toString(),
            cTime.toString(), 0, 0, null, null, null, null, cTime, cTime
        )
    }

    private fun addTimelineMessage(message: Message): Message? {
        synchronized(this) {
            val msg = if (abs(message.cTime - lastMessageTime) > timeLineInterval) {
                newTimelineMessage(message.cTime)
            } else {
                null
            }
            lastMessageTime = message.cTime
            return msg
        }
    }

    private fun addTimelineMessages(messages: List<Message>): List<Message> {
        if (messages.isEmpty()) {
            return ArrayList()
        }
        val newMessages = ArrayList<Message>()
        for (i in messages.indices.reversed()) {
            val msg = addTimelineMessage(messages[i])
            if (msg != null) {
                newMessages.add(msg)
            }
            newMessages.add(messages[i])
        }
        return newMessages.reversed()
    }

    fun setData(messages: List<Message>) {
        synchronized(this) {
            val oldSize = messageList.size
            messageList.clear()
            notifyItemRangeRemoved(0, oldSize)
            lastMessageTime = 0L
            messageList.addAll(addTimelineMessages(messages))
            notifyItemRangeInserted(0, messageList.size)
        }
    }

    fun addData(messages: List<Message>) {
        synchronized(this) {
            val oldSize = messageList.size
            lastMessageTime = messageList[oldSize - 1].cTime
            messageList.addAll(addTimelineMessages(messages))
            notifyItemRangeInserted(oldSize, messages.size)
        }
    }


    fun insertNews(messages: List<Message>) {
        val inserts = mutableListOf<Message>()
        for (m in messages) {
            val pos = findPosition(m)
            if (pos == -1) {
                inserts.add(m)
            } else {
                if (this.messageList[pos].sendStatus != m.sendStatus) {
                    this.messageList[pos].sendStatus = m.sendStatus
                    this.messageList[pos].msgId = m.msgId
                    notifyItemChanged(pos)
                }
            }
        }
        for (m in inserts) {
            insertNew(m)
        }
    }

    fun insertNew(message: Message): Int {
        synchronized(this) {
            val pos = findPosition(message)
            if (pos >= 0 && pos < messageList.size) {
                messageList[pos] = message
                notifyItemChanged(pos)
                return pos
            } else {
                val position = findInsertPosition(message)
                lastMessageTime = if (position >= messageList.size) {
                    0L
                } else {
                    messageList[position].cTime
                }
                val timelineMsg = addTimelineMessage(message)
                if (timelineMsg != null) {
                    messageList.add(position, timelineMsg)
                    messageList.add(position, message)
                    notifyItemRangeInserted(position, 2)
                } else {
                    messageList.add(position, message)
                    notifyItemRangeInserted(position, 1)
                }
                return position
            }
        }
    }

    fun update(message: Message) {
        synchronized(this) {
            val pos = findPosition(message)
            if (pos >= 0 && pos < messageList.size) {
                messageList[pos] = message
                notifyItemChanged(pos)
            }
        }
    }

    private fun findPosition(message: Message): Int {
        synchronized(this) {
            for ((pos, m) in messageList.withIndex()) {
                if (message.id == m.id) {
                    return pos
                }
            }
            return -1
        }
    }


    private fun findInsertPosition(message: Message): Int {
        synchronized(this) {
            for ((pos, m) in messageList.withIndex()) {
                if (message.cTime >= m.cTime) {
                    return pos
                }
            }
            return messageList.size
        }
    }

    fun delete(message: Message) {
        synchronized(this) {
            val pos = findPosition(message)
            if (pos >= 0 && pos < messageList.size) {
                messageList.removeAt(pos)
                notifyItemRemoved(pos)
            }
            // 附加在该消息上的时间线消息也一并删除
            if (pos <= messageList.size - 1 && pos >= 0) {
                if (messageList[pos].type == MsgType.TimeLine.value) {
                    messageList.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }

            val lastPos = pos - 1
            if (lastPos <= messageList.size - 1 && lastPos >= 0) {
                if (messageList[lastPos].type == MsgType.TimeLine.value) {
                    messageList.removeAt(lastPos)
                    notifyItemRemoved(lastPos)
                }
            }
        }
    }

    fun batchDelete(deleteMessages: List<Message>) {
        deleteMessages.forEach {
            delete(it)
        }
    }

    fun getMessageCount(): Int {
        var count = 0
        for (msg in messageList) {
            if (msg.id != 0L) {
                count++
            }
        }
        return count
    }

    fun getMessages(): List<Message> {
        return messageList
    }

    fun setSelectMode(open: Boolean, firstSelected: Message?, recyclerView: RecyclerView) {
        if (this.isSelectMode != open) {
            this.isSelectMode = open
            if (this.isSelectMode) {
                firstSelected?.let {
                    selectedMessages.add(it)
                }
            } else {
                selectedMessages.clear()
            }
            for (i in 0 until messageList.size) {
                val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
                viewHolder?.let {
                    (it as IMBaseMsgVH).updateSelectMode()
                }
            }
        }
    }

    fun highlightFlashing(position: Int, times: Int, recyclerView: RecyclerView) {
        val viewHolder = recyclerView.findViewHolderForLayoutPosition(position)
        viewHolder?.let {
            (it as IMBaseMsgVH).highlightFlashing(times)
        }
    }

    fun getSelectedMessages(): MutableSet<Message> {
        return selectedMessages
    }

    fun isSelectMode(): Boolean {
        return isSelectMode
    }

    fun isItemSelected(message: Message): Boolean {
        return selectedMessages.contains(message)
    }

    fun onSelected(message: Message, selected: Boolean) {
        if (selected) {
            selectedMessages.add(message)
        } else {
            selectedMessages.remove(message)
        }
    }

}