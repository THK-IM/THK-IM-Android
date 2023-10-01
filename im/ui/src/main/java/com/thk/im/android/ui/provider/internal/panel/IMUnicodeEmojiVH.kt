package com.thk.im.android.ui.provider.internal.panel

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R

class IMUnicodeEmojiVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val emojiTextView = itemView.findViewById<AppCompatTextView>(R.id.tv_unicode_emoji)

    fun setUnicodeEmoji(emoji: String) {
        emojiTextView.text = emoji
    }

}