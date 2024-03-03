package com.thk.im.android.ui.protocol

import com.thk.im.android.ui.fragment.IMBaseEmojiFragment

abstract class IMBaseEmojiFragmentProvider {

    abstract fun menuClicked(): Boolean

    abstract fun position(): Int

    abstract fun iconResId(): Int

    abstract fun newFragment(): IMBaseEmojiFragment


}