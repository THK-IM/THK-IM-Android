package com.thk.im.android.core.base.extension

import android.content.res.Resources

fun Int.dp2px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}





