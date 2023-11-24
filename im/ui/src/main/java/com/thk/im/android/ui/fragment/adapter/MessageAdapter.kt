package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.viewholder.BaseMsgVH
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import com.thk.im.android.ui.provider.msg.IMTimeLineMsgIVProvider
import kotlin.math.abs

class MessageAdapter(
    private val session: Session,
    private val lifecycleOwner: LifecycleOwner,
    private val msgVHOperator: IMMsgVHOperator
) :
    RecyclerView.Adapter<BaseMsgVH>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMsgVH {
        val provider = IMUIManager.getMsgIVProviderByViewType(viewType)
        val holder = provider.viewHolder(lifecycleOwner, viewType, parent)
        holder.onCreate()
        return holder
    }

    override fun onBindViewHolder(holder: BaseMsgVH, position: Int) {
        holder.onViewBind(position, messageList, session, msgVHOperator)
    }

    override fun onViewRecycled(holder: BaseMsgVH) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    override fun onViewAttachedToWindow(holder: BaseMsgVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
    }

    override fun onViewDetachedFromWindow(holder: BaseMsgVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetached()
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    private fun newTimelineMessage(cTime: Long): Message {
        return Message(
            0L, 0L, 0L, 0L, IMTimeLineMsgIVProvider.timeLineMsgType, cTime.toString(),
            cTime.toString(), 0, 0, null, null, null, cTime, cTime
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
        if (message.type < 0 && message.type > -1000) {
            return -1
        }
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
        if (message.type < 0 && message.type > -1000) {
            return
        }
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
            if (pos < messageList.size - 1 && pos >= 0) {
                if (messageList[pos].type == com.thk.im.android.ui.provider.msg.IMTimeLineMsgIVProvider.timeLineMsgType) {
                    messageList.removeAt(pos)
                    notifyItemRemoved(pos)
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
                    (it as BaseMsgVH).updateSelectMode()
                }
            }
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