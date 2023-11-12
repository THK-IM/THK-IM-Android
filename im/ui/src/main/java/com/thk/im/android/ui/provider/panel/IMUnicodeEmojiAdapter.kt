package com.thk.im.android.ui.provider.panel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R

class IMUnicodeEmojiAdapter : RecyclerView.Adapter<IMUnicodeEmojiVH>() {

    private var emojis = mutableListOf<String>()
    var onEmojiSelected: OnEmojiSelected? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IMUnicodeEmojiVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itemview_unicode_emoji, parent, false)
        return IMUnicodeEmojiVH(view)
    }

    override fun getItemCount(): Int {
        return emojis.size
    }

    override fun onBindViewHolder(holder: IMUnicodeEmojiVH, position: Int) {
        holder.setUnicodeEmoji(emojis[position])
        holder.itemView.setOnClickListener {
            onEmojiSelected?.onSelected(emojis[position])
        }
    }

    fun setEmoji(emojis: List<String>) {
        val oldSize = this.emojis.size
        this.emojis.addAll(oldSize, emojis)
        notifyItemRangeInserted(oldSize, emojis.size)
    }

    interface OnEmojiSelected {
        fun onSelected(emoji: String)
    }
}