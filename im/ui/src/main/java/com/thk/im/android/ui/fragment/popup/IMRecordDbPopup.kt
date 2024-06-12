package com.thk.im.android.ui.fragment.popup

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.lxj.xpopup.core.BasePopupView
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.ui.R

open class IMRecordDbPopup(context: Context) : BasePopupView(context) {

    override fun onCreate() {
        super.onCreate()
        findViewById<LinearLayout>(R.id.layout_container).setShape(
            Color.parseColor("#A0000000"),
            floatArrayOf(20f, 20f, 20f, 20f),
            false
        )
    }

    override fun getInnerLayoutId(): Int {
        return R.layout.popup_record_db
    }

    fun setIsDismissOnTouchOutside(isTrue: Boolean) {
        popupInfo.isDismissOnTouchOutside = isTrue
    }

    fun setIsDismissOnBackPressed(isTrue: Boolean) {
        popupInfo.isDismissOnBackPressed = isTrue
    }

    fun setTips(tips: String) {
        findViewById<TextView>(R.id.tv_tips).text = tips
    }

    fun setDb(db: Double) {
        val dbView = findViewById<AppCompatImageView>(R.id.iv_db)
        if (db <= 45) {
            dbView.setImageResource(R.drawable.ic_volume_1)
        } else if (db <= 50) {
            dbView.setImageResource(R.drawable.ic_volume_2)
        } else if (db <= 60) {
            dbView.setImageResource(R.drawable.ic_volume_3)
        } else if (db <= 70) {
            dbView.setImageResource(R.drawable.ic_volume_4)
        } else {
            dbView.setImageResource(R.drawable.ic_volume_5)
        }
    }
}