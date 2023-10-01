package com.thk.im.android.ui.provider

import com.thk.im.android.ui.protocol.IMMsgSender
import com.thk.im.android.ui.fragment.panel.BasePanelFragment

abstract class IMBasePanelFragmentProvider {

    abstract fun menuClicked(): Boolean

    abstract fun position(): Int

    abstract fun iconResId(): Int

    abstract fun newFragment(sender: IMMsgSender): BasePanelFragment


}