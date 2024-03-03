package com.thk.im.android.ui.provider.emoji

import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.panel.IMBasePanelFragment
import com.thk.im.android.ui.protocol.IMBaseEmojiFragmentProvider

class IMUnicodeEmojiEmojiProvider(private val position: Int) : IMBaseEmojiFragmentProvider() {

    override fun menuClicked(): Boolean {
        return false
    }

    override fun position(): Int {
        return position
    }

    override fun iconResId(): Int {
        return R.drawable.ic_msg_emoji
    }

    override fun newFragment(): IMBasePanelFragment {
        return IMUnicodeEmojiFragment()
    }
}