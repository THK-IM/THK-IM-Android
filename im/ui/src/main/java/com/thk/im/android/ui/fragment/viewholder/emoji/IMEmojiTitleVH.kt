package com.thk.im.android.ui.fragment.viewholder.emoji

import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.IMBaseEmojiFragmentProvider

class IMEmojiTitleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val menuView = itemView.findViewById<AppCompatImageView>(R.id.iv_menu)
    private var panelProvider: IMBaseEmojiFragmentProvider? = null

    init {
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputBgColor() ?: Color.parseColor("#FFFFFF")
        menuView.setShape(inputTextColor, floatArrayOf(6f, 6f, 6f, 6f), false)
    }

    private fun setIcon(resId: Int) {
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        ContextCompat.getDrawable(itemView.context, resId)?.let {
            it.setTint(inputTextColor)
            menuView.setImageDrawable(it)
        }
    }

    fun setPanelProvider(selected: Boolean, panelProvider: IMBaseEmojiFragmentProvider?) {
        this.panelProvider = panelProvider
        this.panelProvider?.let {
            setIcon(it.iconResId())
        }
        menuView.isSelected = selected
    }
}