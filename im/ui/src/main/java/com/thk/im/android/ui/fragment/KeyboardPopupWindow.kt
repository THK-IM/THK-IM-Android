package com.thk.im.android.ui.fragment

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow

class KeyboardPopupWindow(view: View, val function: (keyboardHeight: Int) -> Unit) :
    PopupWindow(view.context),
    ViewTreeObserver.OnGlobalLayoutListener, View.OnAttachStateChangeListener {

    //当前PopupWindow最大的显示高度
    private var maxHeight = 0

    init {
        contentView = View(view.context)
        width = 0
        height = ViewGroup.LayoutParams.MATCH_PARENT
        //设置背景
        setBackgroundDrawable(ColorDrawable(0))
        //设置键盘弹出模式
        softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED
        //设置监听
        contentView.addOnAttachStateChangeListener(this)
        //显示弹窗
        view.post {
            showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                0,
                0
            )
        }
    }

    private var lastHeight = -1

    override fun onGlobalLayout() {
        val rect = Rect()
        contentView.getWindowVisibleDisplayFrame(rect)
        if (rect.bottom > maxHeight) {
            maxHeight = rect.bottom
        }
        //键盘的高度
        val keyboardHeight = maxHeight - rect.bottom
        if (keyboardHeight != lastHeight) {
            lastHeight = keyboardHeight
            function(keyboardHeight)
        }
    }

    override fun dismiss() {
        super.dismiss()
        contentView.removeOnAttachStateChangeListener(this)
    }

    override fun onViewAttachedToWindow(p0: View) {
        p0.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onViewDetachedFromWindow(p0: View) {
        p0.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

}