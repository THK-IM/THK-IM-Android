package com.thk.im.android.ui.fragment.viewholder

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.provider.IMBasePanelFragmentProvider

class PanelMenuVH(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val menuView = itemView.findViewById<AppCompatImageView>(R.id.iv_menu)
    private var panelProvider: IMBasePanelFragmentProvider? = null

    private fun setIcon(resId: Int) {
        menuView.setImageResource(resId)
    }

    fun setPanelProvider(selected: Boolean, panelProvider: IMBasePanelFragmentProvider?) {
        this.panelProvider = panelProvider
        this.panelProvider?.let {
            setIcon(it.iconResId())
        }
        var scale = 0.7f
        if (selected) {
            scale = 1.0f
        }
        menuView.scaleX = scale
        menuView.scaleY = scale
    }
}