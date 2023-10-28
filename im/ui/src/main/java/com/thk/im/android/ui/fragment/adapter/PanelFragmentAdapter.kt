package com.thk.im.android.ui.fragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thk.im.android.ui.manager.IMUIManager

class PanelFragmentAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return IMUIManager.panelFragmentProviders.size
    }

    override fun createFragment(position: Int): Fragment {
        return IMUIManager.panelFragmentProviders[position]!!.newFragment()
    }

}