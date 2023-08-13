package com.thk.im.android.ui.panel.emoji.bean

import androidx.annotation.Keep

@Keep
class EmoTextBean : EmoBean() {
    var title: String? = null
    var emoImageList: MutableList<EmoImageBean>? = null
}