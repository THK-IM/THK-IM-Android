package com.thk.im.android.core.base.utils

object UIUtils {
    fun setAlphaComponent(color: Int, alpha: Int): Int {
        return if (alpha in 0..255) {
            color.and(16777215).or(alpha.shl(24))
        } else {
            color
        }
    }

    fun setAlphaComponent(color: Int, alpha: Float): Int {
        if (alpha < 0 || alpha > 1.0) {
            return color
        }
        val a = (alpha * 256).toInt()
        return setAlphaComponent(color, a)
    }
}