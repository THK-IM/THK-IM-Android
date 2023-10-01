package com.thk.im.android.ui.provider.internal.panel

import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.panel.BasePanelFragment
import com.thk.im.android.ui.protocol.IMMsgSender
import com.thk.im.android.ui.provider.IMBasePanelFragmentProvider

class IMUnicodeEmojiPanelProvider(private val position: Int) : IMBasePanelFragmentProvider() {

    override fun menuClicked(): Boolean {
        return false
    }

    override fun position(): Int {
        return position
    }

    override fun iconResId(): Int {
        return R.drawable.icon_chat_emoji
    }

    override fun newFragment(sender: IMMsgSender): BasePanelFragment {
        return IMUnicodeEmojiFragment(sender)
    }
}