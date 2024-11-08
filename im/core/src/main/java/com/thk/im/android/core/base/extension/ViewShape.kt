package com.thk.im.android.core.base.extension

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable.Orientation
import android.graphics.drawable.StateListDrawable
import android.view.View
import com.thk.im.android.core.base.utils.ShapeUtils

fun View.setShapeWithStroke(color: Int, strokeColor: Int, strokeWidth: Int, radius: FloatArray) {
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

    background = default
}

fun View.setGradientShape(
    startColor: Int,
    endColor: Int,
    radius: FloatArray,
    horizontal: Boolean = true
) {
    val scale = Resources.getSystem().displayMetrics.density
    val scalePx = FloatArray(radius.size)
    var i = 0
    radius.forEach {
        scalePx[i] = it * scale
        i++
    }

    if (horizontal) {
        background = ShapeUtils.createGradientRectangleDrawable(
            startColor, endColor, Orientation.LEFT_RIGHT, scalePx
        )
    } else {
        background = ShapeUtils.createGradientRectangleDrawable(
            startColor, endColor, Orientation.TOP_BOTTOM, scalePx
        )
    }

}

fun View.setShape(color: Int, radius: FloatArray, autoSelected: Boolean = true) {
    val scale = Resources.getSystem().displayMetrics.density
    val scalePx = FloatArray(radius.size)
    var i = 0
    radius.forEach {
        scalePx[i] = it * scale
        i++
    }
    val default = ShapeUtils.createRectangleDrawable(
        color, color, 0, scalePx
    )

    background = if (autoSelected) {
        val selected = ShapeUtils.createRectangleDrawable(
            (color * 0.5).toInt(),
            (color * 0.5).toInt(),
            (scale).toInt(),
            scalePx
        )
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(android.R.attr.state_pressed), selected)
        drawable.addState(intArrayOf(-android.R.attr.state_checked), default)
        drawable
    } else {
        default
    }
}

fun View.setShapes(color: Int, pressedColor: Int, radius: FloatArray) {
    val scale = Resources.getSystem().displayMetrics.density
    val scalePx = FloatArray(radius.size)
    var i = 0
    radius.forEach {
        scalePx[i] = it * scale
        i++
    }
    val default = ShapeUtils.createRectangleDrawable(
        color, color, 0, scalePx
    )
    val selected = ShapeUtils.createRectangleDrawable(
        pressedColor, pressedColor,
        (scale).toInt(), scalePx
    )

    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_pressed), selected)
    drawable.addState(intArrayOf(-android.R.attr.state_checked), default)
    background = drawable
}

fun View.setShape(color: Int, selectColor: Int, radius: FloatArray) {
    val scale = Resources.getSystem().displayMetrics.density
    val scalePx = FloatArray(radius.size)
    var i = 0
    radius.forEach {
        scalePx[i] = it * scale
        i++
    }
    val default = ShapeUtils.createRectangleDrawable(
        color, color, 0, scalePx
    )
    val selected = ShapeUtils.createRectangleDrawable(
        selectColor, selectColor, 0, scalePx
    )
    val drawable = StateListDrawable()
    drawable.addState(intArrayOf(android.R.attr.state_pressed), selected)
    drawable.addState(intArrayOf(android.R.attr.state_selected), selected)
    drawable.addState(intArrayOf(-android.R.attr.state_checked), default)
    background = drawable
}