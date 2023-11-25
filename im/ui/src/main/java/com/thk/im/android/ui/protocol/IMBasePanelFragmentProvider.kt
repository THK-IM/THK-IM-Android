package com.thk.im.android.ui.protocol

import com.thk.im.android.ui.fragment.panel.IMBasePanelFragment

abstract class IMBasePanelFragmentProvider {

    abstract fun menuClicked(): Boolean

    abstract fun position(): Int

    abstract fun iconResId(): Int

    abstract fun newFragment(): IMBasePanelFragment


}