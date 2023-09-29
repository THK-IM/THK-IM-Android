package com.thk.im.android.ui.fragment.adapter

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.manager.IMItemViewManager
import com.thk.im.android.ui.viewholder.msg.BaseMsgVH
import kotlin.math.abs

class MessageAdapter(
    private val session: Session,
    private val lifecycleOwner: LifecycleOwner,
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<BaseMsgVH>() {

    private var lastMessageTime: Long = 0L
    private val timeLineMsgType = 9999
    private val timeLineInterval = 5 * 60 * 1000

    private val messageList = mutableListOf<Message>()

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val provider = IMItemViewManager.getMsgIVProviderByMsgType(message.type)
        return provider.viewType(message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMsgVH {
        val provider = IMItemViewManager.getMsgIVProviderByViewType(viewType)
        val holder =  provider.viewHolder(lifecycleOwner, viewType, parent)
        holder.resetLayout()
        return holder
    }

    override fun onBindViewHolder(holder: BaseMsgVH, position: Int) {
        val message = messageList[position]
        holder.onViewBind(message, session)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }


    private fun newTimelineMessage(cTime: Long): Message {
        return Message(
            0L, 0L, 0L, 0L, timeLineMsgType, cTime.toString(),
            0, 0, cTime, 0L, "", null, null
        )
    }

    private fun addTimelineMessage(message: Message): Message? {
        val msg = if (abs(message.cTime - lastMessageTime) > timeLineInterval) {
            newTimelineMessage(message.cTime)
        } else {
            null
        }
        lastMessageTime = message.cTime
        return msg
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
//        if (newMessages[0].type != timeLineMsgType) {
//            val message = newTimelineMessage(newMessages[0].cTime)
//            newMessages.add(0, message)
//        }
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
                    messageList.add(position, message)
                    messageList.add(position + 1, timelineMsg)
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
        for ((pos, m) in messageList.withIndex()) {
            if (message.id == m.id) {
                return pos
            }
        }
        return -1
    }


    private fun findInsertPosition(message: Message): Int {
        for ((pos, m) in messageList.withIndex()) {
            if (message.cTime >= m.cTime) {
                return pos
            }
        }
        return messageList.size
    }

    fun delete(message: Message) {
        synchronized(this) {
            val pos = findPosition(message)
            if (pos >= 0) {
                messageList.removeAt(pos)
                notifyItemRemoved(pos)
            }
            val timeLinePost = pos + 1
            // 附加在该消息上的时间线消息也一并删除
            if (timeLinePost < messageList.size - 1) {
                if (messageList[timeLinePost].type == timeLineMsgType) {
                    messageList.removeAt(timeLinePost)
                    notifyItemRemoved(timeLinePost)
                }
            }
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

}