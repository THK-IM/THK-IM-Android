package com.thk.im.android.ui.base.loading

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import com.lxj.xpopup.core.BasePopupView
import com.thk.im.android.R
import com.thk.im.android.core.base.extension.setShape

open class PopupLoading(context: Context) : BasePopupView(context) {

    override fun onCreate() {
        super.onCreate()
        findViewById<LinearLayout>(R.id.layout_container).setShape(
            Color.parseColor("#24000000"),
            Color.parseColor("#24000000"),
            0,
            floatArrayOf(6f, 6f, 6f, 6f)
        )
    }
    override fun getInnerLayoutId(): Int {
        return R.layout.layout_loading
    }

    fun setIsDismissOnTouchOutside(isTrue: Boolean) {
        popupInfo.isDismissOnTouchOutside = isTrue
    }

    fun setIsDismissOnBackPressed(isTrue: Boolean) {
        popupInfo.isDismissOnBackPressed = isTrue
    }
}