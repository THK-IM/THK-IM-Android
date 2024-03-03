package com.thk.im.android.ui.msg.viewholder

import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.ui.R
import com.thk.im.android.ui.protocol.IMBaseEmojiFragmentProvider

class PanelMenuVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val menuView = itemView.findViewById<AppCompatImageView>(R.id.iv_menu)
    private var panelProvider: IMBaseEmojiFragmentProvider? = null

    init {
        menuView.setShape(
            Color.TRANSPARENT,
            Color.parseColor("#E0E0E0"),
            floatArrayOf(4f, 4f, 4f, 4f)
        )
    }
    private fun setIcon(resId: Int) {
        menuView.setImageResource(resId)
    }

    fun setPanelProvider(selected: Boolean, panelProvider: IMBaseEmojiFragmentProvider?) {
        this.panelProvider = panelProvider
        this.panelProvider?.let {
            setIcon(it.iconResId())
        }
        menuView.isSelected = selected
    }
}