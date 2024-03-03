package com.thk.im.android.ui.fragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thk.im.android.ui.manager.IMUIManager

class EmojiFragmentAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return IMUIManager.emojiFragmentProviders.size
    }

    override fun createFragment(position: Int): Fragment {
        return IMUIManager.emojiFragmentProviders[position]!!.newFragment()
    }

}