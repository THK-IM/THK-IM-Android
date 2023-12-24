package com.thk.im.android.ui.main.adpater

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thk.im.android.MessageActivity
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.fragment.IMSessionFragment

class MainFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            IMSessionFragment()
        } else {
            Fragment()
        }
    }
}