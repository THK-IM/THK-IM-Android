package com.thk.im.android.ui.panel.emoji

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.thk.im.android.ui.R
import com.thk.im.android.ui.panel.IPanel

/**
 * 表情面板
 */
class EmojiPanel(val activity: FragmentActivity, val panel: View, private val emojiPanelCallback: EmojiPanelCallback? = null) :
    IPanel {

    var viewPager2: ViewPager2 = panel.findViewById(R.id.emo_viewpager)

    init {
        initPanel()
    }

    private fun initPanel() {
        viewPager2.adapter =
            EmojiFragmentStateAdapter(activity, emojiPanelCallback)
    }

    override fun show() {
        panel.visibility = View.VISIBLE
    }

    override fun hide() {
        panel.visibility = View.GONE
    }


}