package com.thk.im.preview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.thk.im.android.core.base.utils.AppUtils
import kotlin.math.min

class CircleProgressBar: View {

    private val circlePaint  = Paint()
    private val progressPaint = Paint()
    private val textPaint = Paint()
    private val bounds = Rect()
    private var progress = 0

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        circlePaint.color = Color.parseColor("#40000000")
        circlePaint.style = Paint.Style.FILL
        circlePaint.strokeWidth = 8f

        progressPaint.color = Color.parseColor("#EEEEEE")
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = 8f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = AppUtils.dp2px(12f).toFloat()
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2
        val centerY = height / 2
        val radius = (min(centerX.toDouble(), centerY.toDouble()) - 20).toInt()

        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), circlePaint)
        val sweepAngle = (360 * progress / 100).toFloat()
        canvas.drawArc(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat(),
            -90f,
            sweepAngle,
            false,
            progressPaint
        )

        val progressText = "$progress"
        textPaint.getTextBounds(progressText, 0, progressText.length, bounds)
        canvas.drawText(
            progressText, centerX.toFloat(), (centerY + bounds.height() / 2).toFloat(),
            textPaint
        )
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate() // 通知视图进行重绘
    }
}