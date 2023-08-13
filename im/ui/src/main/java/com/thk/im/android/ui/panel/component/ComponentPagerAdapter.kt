package com.thk.im.android.ui.panel.component

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thk.im.android.ui.panel.component.internal.UIComponentManager

/**
 * 组件分页适配器
 */
class ComponentPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val uiComponentManager: UIComponentManager
) :
    FragmentStateAdapter(fragmentActivity) {

    companion object {
        const val PAGE_NUMBER = 8
    }

    override fun getItemCount(): Int {
        return if (uiComponentManager.size <= PAGE_NUMBER) {
            1
        } else if (uiComponentManager.size % PAGE_NUMBER == 0) {
            uiComponentManager.size / PAGE_NUMBER
        } else {
            (uiComponentManager.size / PAGE_NUMBER) + 1
        }
    }

    override fun createFragment(position: Int): Fragment {
        return ComponentPanelFragment.getInstance(position, uiComponentManager)
    }
}