package com.thk.im.android.core.base.extension

import android.content.res.Resources
import android.graphics.drawable.StateListDrawable
import android.view.View
import com.thk.im.android.core.base.utils.ShapeUtils

fun View.setShape(strokeColor: Int, color: Int, strokeWidth: Int, radius: FloatArray, autoSelected: Boolean = true) {
    val scale = Resources.getSystem().displayMetrics.density
    val scalePx = FloatArray(radius.size)
    var i = 0
    radius.forEach {
        scalePx[i] = it * scale
        i++
    }
    val default = ShapeUtils.createRectangleDrawable(
        color, strokeColor, (strokeWidth * scale).toInt(), scalePx
    )

    background = if (autoSelected) {
        val selected = ShapeUtils.createRectangleDrawable(
            (color * 0.5).toInt(), (strokeColor * 0.5).toInt(), (strokeWidth * scale).toInt(), scalePx
        )
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(android.R.attr.state_pressed), selected)
        drawable.addState(intArrayOf(-android.R.attr.state_checked), default)
        drawable
    } else {
        default
    }
}