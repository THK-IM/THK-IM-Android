package com.thk.im.android.ui.component

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import androidx.emoji2.widget.EmojiTextView
import com.lxj.xpopup.core.PositionPopupView
import com.thk.im.android.R
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.live.BeingRequestedSignal

class BeRequestedCallPopup(context: Context) : PositionPopupView(context) {

    lateinit var signal: BeingRequestedSignal

    private lateinit var rootView: LinearLayout
    private lateinit var msgView: EmojiTextView
    private lateinit var acceptView: EmojiTextView
    private lateinit var rejectView: EmojiTextView

    override fun getImplLayoutId(): Int {
        return R.layout.popup_be_requesting
    }

    override fun onCreate() {
        super.onCreate()
        rootView = findViewById(R.id.ly_root)
        msgView = findViewById(R.id.tv_msg)
        acceptView = findViewById(R.id.tv_accept)
        rejectView = findViewById(R.id.tv_reject)
        rootView.setShape(
            Color.parseColor("#FFFFFF"), floatArrayOf(12f, 12f, 12f, 12f), false
        )
        msgView.text = signal.msg
        acceptView.setShape(
            Color.parseColor("#DDDDDD"), floatArrayOf(12f, 12f, 12f, 12f), false
        )

        rejectView.setShape(
            Color.parseColor("#DDDDDD"), floatArrayOf(12f, 12f, 12f, 12f), false
        )

        acceptView.setOnClickListener {

        }

        rejectView.setOnClickListener {

        }
    }

    override fun getPopupWidth(): Int {
        return AppUtils.instance().screenWidth
    }

}