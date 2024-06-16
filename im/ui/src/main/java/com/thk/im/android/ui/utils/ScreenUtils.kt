package com.thk.im.android.ui.utils

import android.app.Activity
import android.content.Context
import android.os.Build

object ScreenUtils {

    fun isMultiWindowMode(ctx: Context): Boolean {
        val activity = ctx as? Activity? ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return activity.isInMultiWindowMode
        }
        return false
    }
}