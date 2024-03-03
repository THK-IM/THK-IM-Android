package com.thk.im.android.ui.fragment.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min


class IMReadStatusView : View {

    private var color = Color.TRANSPARENT
    private var progress: Float = 0f
    private var lineWidth: Float = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    fun updateStatus(color: Int, lineWidth: Float, progress: Float) {
        this.color = color
        this.progress = progress
        this.lineWidth = lineWidth * Resources.getSystem().displayMetrics.density
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        this.drawCircle(canvas)
        if (this.progress >= 1.0) {
            this.drawAlready(canvas)
        } else if (this.progress > 0.0) {
            this.drawNotReady(canvas)
        }
    }

    private fun drawCircle(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidth / 2
        val maxRadius = min(width, height) / 2f
        val radius = maxRadius - lineWidth / 2f
        val centerX = width / 2f
        val centerY = height / 2f
        val oval = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(oval, 0f, 360f, true, paint)
    }

    private fun drawNotReady(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.strokeWidth = lineWidth / 2

        val oval = RectF()
        val maxRadius = min(width, height) / 2f
        val radius = maxRadius - lineWidth / 2f - maxRadius / 3
        val centerX = width / 2f
        val centerY = height / 2f
        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        val startAngle = -90f
        val sweepAngle = 360 * min(1f, progress)
        canvas.drawArc(oval, startAngle, sweepAngle, true, paint)
    }

    private fun drawAlready(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidth / 2
        val path = Path()
        val startX = width / 4f
        val startY = height / 2f
        val middleX = width / 2f
        val middleY = height * 3 / 4f
        val endX = width * 3 / 4f
        val endY = height / 4f
        path.moveTo(startX, startY)
        path.lineTo(middleX, middleY)
        path.lineTo(endX, endY)
        canvas.drawPath(path, paint)
    }

}