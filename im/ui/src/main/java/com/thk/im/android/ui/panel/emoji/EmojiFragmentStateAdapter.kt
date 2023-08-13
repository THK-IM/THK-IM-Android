package com.thk.im.android.ui.panel.emoji

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EmojiFragmentStateAdapter(
    private val activity: FragmentActivity,
    private val emojiPanelCallback: EmojiPanelCallback?
) :
    FragmentStateAdapter(activity) {


    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EmojiFragment(emojiPanelCallback)
            else -> EmojiImgFragment()
        }
    }

    fun update() {
        for (fragment in activity.supportFragmentManager.fragments) {
            if (fragment is EmojiFragment) {
                fragment.updateData()
            }
        }
    }
}