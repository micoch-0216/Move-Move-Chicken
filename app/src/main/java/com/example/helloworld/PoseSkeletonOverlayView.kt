package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.tasks.vision.core.RunningMode

/**
 * 在相機畫面上繪製 MediaPipe 姿態骨架。
 * 骨架填滿整個螢幕；關節點經 EMA 平滑以穩定追蹤。
 */
class PoseSkeletonOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 是否鏡像 X 軸（方向相反時設為 false） */
    var mirrorForFrontCamera: Boolean = false

    /** 平滑係數 (0~1)：越大越即時、越小越平滑；0.95 接近即時跟隨動作 */
    var smoothingAlpha: Float = 0.95f
        set(value) { field = value.coerceIn(0.01f, 1f) }

    private val lineStrokeWidth = 8f
    private val pointRadius = 10f

    private fun createLinePaint(hexColor: String) = Paint().apply {
        color = Color.parseColor(hexColor)
        style = Paint.Style.STROKE
        strokeWidth = lineStrokeWidth
        isAntiAlias = true
    }

    private val faceLinePaint = createLinePaint("#FFFF00")
    private val torsoLinePaint = createLinePaint("#00FF00")
    private val leftLimbLinePaint = createLinePaint("#FF0000")
    private val rightLimbLinePaint = createLinePaint("#00FFFF")

    private val pointPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var results: PoseLandmarkerResult? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var runningMode: RunningMode = RunningMode.LIVE_STREAM

    /** 平滑後的關節點 (x,y)，與 MediaPipe 33 點對應；null 表示尚未有資料 */
    private val smoothedLandmarks = Array<Pair<Float, Float>?>(33) { null }

    private val faceConnections = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 7, 0 to 4, 4 to 5, 5 to 6, 6 to 8, 9 to 10
    )
    private val torsoConnections = listOf(
        11 to 12, 11 to 23, 12 to 24, 23 to 24
    )
    private val leftLimbConnections = listOf(
        11 to 13, 13 to 15, 15 to 17, 15 to 19, 15 to 21, 17 to 19,
        23 to 25, 25 to 27, 27 to 29, 29 to 31, 27 to 31
    )
    private val rightLimbConnections = listOf(
        12 to 14, 14 to 16, 16 to 18, 16 to 20, 16 to 22, 18 to 20,
        24 to 26, 26 to 28, 28 to 30, 30 to 32, 28 to 32
    )

    fun setResults(
        poseLandmarkerResult: PoseLandmarkerResult?,
        imageWidth: Int,
        imageHeight: Int,
        runningMode: RunningMode = RunningMode.LIVE_STREAM
    ) {
        this.results = poseLandmarkerResult
        this.imageWidth = imageWidth.coerceAtLeast(1)
        this.imageHeight = imageHeight.coerceAtLeast(1)
        this.runningMode = runningMode
        invalidate()
    }

    /**
     * 歸一化座標 (0~1) 轉為整個螢幕座標；可選 X 軸鏡像。
     */
    private fun normToView(xNorm: Float, yNorm: Float): Pair<Float, Float> {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return Pair(xNorm, yNorm)
        val x = if (mirrorForFrontCamera) (1f - xNorm) * w else xNorm * w
        val y = yNorm * h
        return Pair(x, y)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = results ?: return
        val landmarksList = result.landmarks()
        if (landmarksList.isEmpty()) return

        val landmarks = landmarksList[0]
        if (landmarks.size < 11) return

        val alpha = smoothingAlpha
        for (i in landmarks.indices) {
            if (i >= smoothedLandmarks.size) break
            val nx = landmarks[i].x()
            val ny = landmarks[i].y()
            val prev = smoothedLandmarks[i]
            smoothedLandmarks[i] = if (prev == null) {
                Pair(nx, ny)
            } else {
                Pair(
                    prev.first + alpha * (nx - prev.first),
                    prev.second + alpha * (ny - prev.second)
                )
            }
        }

        fun drawConnections(connections: List<Pair<Int, Int>>, paint: Paint) {
            for ((start, end) in connections) {
                if (start >= smoothedLandmarks.size || end >= smoothedLandmarks.size) continue
                val sp = smoothedLandmarks[start] ?: continue
                val ep = smoothedLandmarks[end] ?: continue
                val (sx, sy) = normToView(sp.first, sp.second)
                val (ex, ey) = normToView(ep.first, ep.second)
                canvas.drawLine(sx, sy, ex, ey, paint)
            }
        }

        drawConnections(faceConnections, faceLinePaint)
        drawConnections(torsoConnections, torsoLinePaint)
        drawConnections(leftLimbConnections, leftLimbLinePaint)
        drawConnections(rightLimbConnections, rightLimbLinePaint)

        for (i in smoothedLandmarks.indices) {
            val p = smoothedLandmarks[i] ?: continue
            val (px, py) = normToView(p.first, p.second)
            canvas.drawCircle(px, py, pointRadius, pointPaint)
        }
    }
}
