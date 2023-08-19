package com.thk.im.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.R

class MessageAdapter(private val lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<MessageVH>() {

    private val messages = mutableListOf<String>()
    fun setData(messages: List<String>) {
        val oldSize = this.messages.size
        if (oldSize > 0) {
            this.messages.clear()
            notifyItemRangeRemoved(0, oldSize)
        }
        this.messages.addAll(messages)
        notifyItemRangeInserted(0, this.messages.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_rtc_message, parent, false)
        return MessageVH(lifecycleOwner, itemView)
    }

    override fun getItemCount(): Int {
        return this.messages.size
    }

    override fun onBindViewHolder(holder: MessageVH, position: Int) {
        holder.bind(messages[position])
    }

    fun addData(msg: String) {
        val oldSize = messages.size
        this.messages.add(msg)
        notifyItemInserted(oldSize)
    }

}