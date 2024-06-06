package com.thk.im.android.core.base.utils

import android.graphics.Path
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

    fun roundedRect(
        left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float,
        tl: Boolean = true, tr: Boolean = true, br: Boolean = true, bl: Boolean = true): Path {
        var rx = rx
        var ry = ry
        val path = Path()
        if (rx < 0) rx = 0f
        if (ry < 0) ry = 0f
        val width = right - left
        val height = bottom - top
        if (rx > width / 2) rx = width / 2
        if (ry > height / 2) ry = height / 2
        val widthMinusCorners = width - 2 * rx
        val heightMinusCorners = height - 2 * ry

        path.moveTo(right, top + ry)
        if (tr)
            path.rQuadTo(0f, -ry, -rx, -ry)//top-right corner
        else {
            path.rLineTo(0f, -ry)
            path.rLineTo(-rx, 0f)
        }
        path.rLineTo(-widthMinusCorners, 0f)
        if (tl)
            path.rQuadTo(-rx, 0f, -rx, ry) //top-left corner
        else {
            path.rLineTo(-rx, 0f)
            path.rLineTo(0f, ry)
        }
        path.rLineTo(0f, heightMinusCorners)

        if (bl)
            path.rQuadTo(0f, ry, rx, ry)//bottom-left corner
        else {
            path.rLineTo(0f, ry)
            path.rLineTo(rx, 0f)
        }

        path.rLineTo(widthMinusCorners, 0f)
        if (br)
            path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, -ry)
        }

        path.rLineTo(0f, -heightMinusCorners)

        path.close()//Given close, last lineto can be removed.

        return path
    }

    fun roundedRect(
        left: Float, top: Float, right: Float, bottom: Float,
        ttl: Float, ttr: Float, btr: Float, btl: Float): Path {
        var tl = ttl
        var tr = ttr
        var br = btr
        var bl = btl
        val path = Path()
        if (tl < 0) tl = 0f
        if (tr < 0) tr = 0f
        if (br < 0) br = 0f
        if (bl < 0) bl = 0f
        val width = right - left
        val height = bottom - top
        val min = Math.min(width, height)
        if (tl > min / 2) tl = min / 2
        if (tr > min / 2) tr = min / 2
        if (br > min / 2) br = min / 2
        if (bl > min / 2) bl = min / 2
//        val widthMinusCorners = width - 2 * rx
//        val heightMinusCorners = height - 2 * ry
        if (tl == tr && tr == br && br == bl && tl == min / 2) {
            val radius = min / 2F
            path.addCircle(left + radius, top + radius, radius, Path.Direction.CW)
            return path
        }

        path.moveTo(right, top + tr)
        if (tr > 0)
            path.rQuadTo(0f, -tr, -tr, -tr)//top-right corner
        else {
            path.rLineTo(0f, -tr)
            path.rLineTo(-tr, 0f)
        }
        path.rLineTo(-(width - tr - tl), 0f)
        if (tl > 0)
            path.rQuadTo(-tl, 0f, -tl, tl) //top-left corner
        else {
            path.rLineTo(-tl, 0f)
            path.rLineTo(0f, tl)
        }
        path.rLineTo(0f, height - tl - bl)

        if (bl > 0)
            path.rQuadTo(0f, bl, bl, bl)//bottom-left corner
        else {
            path.rLineTo(0f, bl)
            path.rLineTo(bl, 0f)
        }

        path.rLineTo(width - bl - br, 0f)
        if (br > 0)
            path.rQuadTo(br, 0f, br, -br) //bottom-right corner
        else {
            path.rLineTo(br, 0f)
            path.rLineTo(0f, -br)
        }

        path.rLineTo(0f, -(height - br - tr))

        path.close()//Given close, last lineto can be removed.

        return path
    }


}