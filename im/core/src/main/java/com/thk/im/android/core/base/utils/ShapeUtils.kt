package com.thk.im.android.core.base.utils

import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt


object ShapeUtils {

    /**
     * 创建背景颜色
     *
     * @param color       填充色
     * @param strokeColor 线条颜色
     * @param strokeWidth 线条宽度  单位px
     * @param radius      角度  px,长度为4,分别表示左上,右上,右下,左下的角度
     */
    fun createRectangleDrawable(
        @ColorInt color: Int,
        @ColorInt strokeColor: Int,
        strokeWidth: Int,
        radius: FloatArray?
    ): GradientDrawable {
        val radiusBg = GradientDrawable()
        //设置Shape类型
        radiusBg.shape = GradientDrawable.RECTANGLE
        //设置填充颜色
        radiusBg.setColor(color)
        //设置线条粗心和颜色,px
        radiusBg.setStroke(strokeWidth, strokeColor)
        //每连续的两个数值表示是一个角度,四组:左上,右上,右下,左下
        if (radius != null && radius.size == 4) {
            radiusBg.cornerRadii = floatArrayOf(
                radius[0], radius[0],
                radius[1], radius[1],
                radius[2], radius[2],
                radius[3], radius[3]
            )
        }
        return radiusBg
    }

}