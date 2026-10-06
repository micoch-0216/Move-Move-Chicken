package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 在相機畫面上繪製 MediaPipe 臉部偵測框。
 */
class FaceDetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    /** 要繪製的矩形（歸一化 0~1，由外部設定） */
    var detections: List<RectF> = emptyList()
        set(value) {
            field = value
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        for (rect in detections) {
            val left = rect.left * w
            val top = rect.top * h
            val right = rect.right * w
            val bottom = rect.bottom * h
            canvas.drawRect(left, top, right, bottom, boxPaint)
        }
    }
}
