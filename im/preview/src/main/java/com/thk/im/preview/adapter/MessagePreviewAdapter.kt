package com.thk.im.preview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.preview.R
import com.thk.im.preview.viewholder.ImagePreviewVH
import com.thk.im.preview.viewholder.PreviewVH
import com.thk.im.preview.viewholder.VideoPreviewVH

class MessagePreviewAdapter(private val lifecycleOwner: LifecycleOwner, items: List<Message>) :
    RecyclerView.Adapter<PreviewVH>() {


    private val messages = mutableListOf<Message>()

    init {
        messages.addAll(items)
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewVH {
        return if (viewType == MsgType.IMAGE.value) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_media_image, parent, false)
            ImagePreviewVH(lifecycleOwner, view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemview_media_video, parent, false)
            VideoPreviewVH(lifecycleOwner, view)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: PreviewVH, position: Int) {
        holder.bindMessage(messages[position])
    }

    override fun onViewRecycled(holder: PreviewVH) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    override fun onViewAttachedToWindow(holder: PreviewVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
    }

    override fun onViewDetachedFromWindow(holder: PreviewVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetached()
    }

    fun onPageSelected(position: Int, recyclerView: RecyclerView) {
        for (i in 0 until messages.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (position == i) {
                    (viewHolder as PreviewVH).startPreview()
                } else {
                    (viewHolder as PreviewVH).stopPreview()
                }
            }
        }
    }

    fun hideChildren(currentItem: Int, recyclerView: RecyclerView) {
        for (i in 0 until messages.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (currentItem != i) {
                    (viewHolder as PreviewVH).hide()
                }
            }
        }
    }

    fun showChildren(currentItem: Int, recyclerView: RecyclerView) {
        for (i in 0 until messages.size) {
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i)
            viewHolder?.let {
                if (currentItem != i) {
                    (viewHolder as PreviewVH).show()
                }
            }
        }
    }

    fun updateMessage(message: Message) {
        for (i in 0 until messages.size) {
            if (messages[i].id == message.id) {
                messages[i] = message
                notifyItemChanged(i)
                break
            }
        }
    }

    fun getMessage(position: Int): Message? {
        return if (position < messages.size) {
            messages[position]
        } else {
            null
        }
    }

    fun addOlderMessage(messages: List<Message>, older: Boolean) {
        var pos = 0
        if (!older) {
            pos = this.messages.size
        }
        this.messages.addAll(pos, messages)
        notifyItemRangeInserted(pos, messages.size)
    }

    fun getMessages(): List<Message> {
        return this.messages
    }
}