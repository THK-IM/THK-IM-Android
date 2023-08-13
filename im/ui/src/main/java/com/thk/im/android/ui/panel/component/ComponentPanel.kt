package com.thk.im.android.ui.panel.component

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.thk.im.android.ui.R
import com.thk.im.android.ui.panel.IPanel
import com.thk.im.android.ui.panel.component.internal.UIComponentManager

/**
 * 组件面板
 */
class ComponentPanel(
    val activity: FragmentActivity,
    val itemView: View,
    private val uiComponentManager: UIComponentManager
) : IPanel {

    init {
        initPanel()
    }

    private fun initPanel() {
        val adapter = ComponentPagerAdapter(
            activity, uiComponentManager
        )
        val viewPager2 = itemView.findViewById<ViewPager2>(R.id.viewpager2)
        viewPager2.adapter = adapter
    }

    override fun show() {
        itemView.visibility = View.VISIBLE
    }

    override fun hide() {
        itemView.visibility = View.GONE
    }


}