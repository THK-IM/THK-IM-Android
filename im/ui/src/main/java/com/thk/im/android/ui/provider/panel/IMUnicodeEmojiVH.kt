package com.thk.im.android.ui.provider.panel

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.emoji2.widget.EmojiTextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R

class IMUnicodeEmojiVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val emojiTextView = itemView.findViewById<EmojiTextView>(R.id.tv_unicode_emoji)

    fun setUnicodeEmoji(emoji: String) {
        emojiTextView.text = emoji
    }

}