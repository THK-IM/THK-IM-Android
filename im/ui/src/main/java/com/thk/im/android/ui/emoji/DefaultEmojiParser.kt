package com.thk.im.android.ui.emoji

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.core.content.ContextCompat
import com.thk.im.android.common.extension.dp2px
import com.thk.im.android.ui.R
import com.thk.im.android.ui.emoji.span.CenterImageSpan
import java.util.regex.Pattern

open class DefaultEmojiParser(val context: Context) : IEmojiParser {


    private var mNewEmoPattern: Pattern? = null

    init {
        mNewEmoPattern = buildNewEmoPattern()

    }


    private fun buildNewEmoPattern(): Pattern {
        val patternString = StringBuilder(getEmojiTags().size * 3)
        patternString.append('(')
        for (s in getEmojiTags()) {
            patternString.append(Pattern.quote(s))
            patternString.append('|')
        }
        patternString.replace(
            patternString.length - 1,
            patternString.length, ")"
        )
        return Pattern.compile(patternString.toString())
    }


    override fun getEmojiIds(): IntArray {
        return intArrayOf(
        )
    }

    override fun getEmojiTags(): Array<String> {
        return context.resources.getStringArray(R.array.new_emo_phrase)
    }

    override fun getIndexFromResId(emoId: Int): Int {
        for (i in getEmojiIds().indices) {
            if (emoId == getEmojiIds()[i]) {
                return i
            }
        }
        return -1
    }

    override fun getTagFromResId(resId: Int): String {
        for (i in getEmojiIds().indices) {
            if (resId == getEmojiIds()[i]) {
                if (i < getEmojiTags().size) {
                    return getEmojiTags()[i]
                }
            }
        }
        return ""
    }

    override fun getEmoResIdFromTag(tag: String): Int {
        for (i in getEmojiTags().indices) {
            if (tag.isNotEmpty() && tag == getEmojiTags()[i] && i < getEmojiIds().size) {
                return getEmojiIds()[i]
            }
        }
        return -1
    }

    override fun emoCharSequence(text: CharSequence): CharSequence {
        val builder = SpannableStringBuilder(text)
        val matcher = mNewEmoPattern!!.matcher(text)
        while (matcher.find()) {
            val resId = EmojiManager.parser.getEmoResIdFromTag(matcher.group())
            if (resId == -1) {
                continue
            }
            val drawable = ContextCompat.getDrawable(context, resId)
            val size: Int = 32.dp2px()
            if (drawable != null) {
                drawable.setBounds(0, 0, size, size)
                val imageSpan = CenterImageSpan(drawable)
                builder.setSpan(
                    imageSpan, matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return builder
    }
}