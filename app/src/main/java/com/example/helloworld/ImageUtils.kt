package com.example.helloworld

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * 將 CameraX ImageProxy 轉成 Bitmap，供 MediaPipe 使用。
 * 支援 RGBA_8888（單平面）與 YUV_420_888（正確處理 rowStride）。
 */
fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return when {
        imageProxy.planes.size == 1 && imageProxy.format != ImageFormat.YUV_420_888 ->
            imageProxyToBitmapFromRgba(imageProxy)
        imageProxy.format == ImageFormat.YUV_420_888 && imageProxy.planes.size >= 3 ->
            imageProxyToBitmapFromYuv(imageProxy)
        else -> null
    }
}

/** RGBA_8888 單平面：複製到 Bitmap（處理 rowStride） */
private fun imageProxyToBitmapFromRgba(imageProxy: ImageProxy): Bitmap? {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer.duplicate()
    buffer.rewind()
    val pixelStride = plane.pixelStride
    val rowStride = plane.rowStride
    val w = imageProxy.width
    val h = imageProxy.height
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    if (rowStride == w * pixelStride) {
        bitmap.copyPixelsFromBuffer(buffer)
    } else {
        val row = ByteArray(rowStride)
        val pixels = IntArray(w)
        for (y in 0 until h) {
            buffer.get(row)
            var i = 0
            var j = 0
            while (j < w) {
                val r = row[i].toInt() and 0xff
                val g = row[i + 1].toInt() and 0xff
                val b = row[i + 2].toInt() and 0xff
                val a = row[i + 3].toInt() and 0xff
                pixels[j++] = (a shl 24) or (r shl 16) or (g shl 8) or b
                i += pixelStride
            }
            bitmap.setPixels(pixels, 0, w, 0, y, w, 1)
        }
    }
    return applyRotationAndMirror(bitmap, imageProxy.imageInfo.rotationDegrees, true)
}

/** YUV_420_888：依 rowStride/pixelStride 組出緊湊 NV21 再轉 JPEG → Bitmap */
private fun imageProxyToBitmapFromYuv(imageProxy: ImageProxy): Bitmap? {
    val w = imageProxy.width
    val h = imageProxy.height
    val yPlane = imageProxy.planes[0]
    val uPlane = imageProxy.planes[1]
    val vPlane = imageProxy.planes[2]
    val yRowStride = yPlane.rowStride
    val yPixelStride = yPlane.pixelStride
    val uRowStride = uPlane.rowStride
    val vRowStride = vPlane.rowStride
    val uPixelStride = uPlane.pixelStride
    val vPixelStride = vPlane.pixelStride
    val yLen = w * h
    val uvLen = (w / 2) * (h / 2)
    val nv21 = ByteArray(yLen + uvLen * 2)
    val yBuffer = yPlane.buffer
    var yPos = 0
    for (row in 0 until h) {
        yBuffer.position(row * yRowStride)
        for (col in 0 until w) {
            nv21[yPos++] = yBuffer.get()
            if (yPixelStride == 2 && col < w - 1) yBuffer.get()
        }
    }
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer
    var uvPos = yLen
    val uvH = h / 2
    val uvW = w / 2
    for (row in 0 until uvH) {
        uBuffer.position(row * uRowStride)
        vBuffer.position(row * vRowStride)
        for (col in 0 until uvW) {
            nv21[uvPos++] = vBuffer.get()
            nv21[uvPos++] = uBuffer.get()
            if (vPixelStride == 2 && col < uvW - 1) vBuffer.get()
            if (uPixelStride == 2 && col < uvW - 1) uBuffer.get()
        }
    }
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, w, h, null)
    val out = ByteArrayOutputStream()
    if (!yuvImage.compressToJpeg(Rect(0, 0, w, h), 85, out)) return null
    val jpegBytes = out.toByteArray()
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size) ?: return null
    return applyRotationAndMirror(bitmap, imageProxy.imageInfo.rotationDegrees, true)
}

private fun applyRotationAndMirror(bitmap: Bitmap, rotationDegrees: Int, mirror: Boolean): Bitmap {
    if (rotationDegrees == 0 && !mirror) return bitmap
    val matrix = Matrix()
    if (rotationDegrees != 0) matrix.postRotate(rotationDegrees.toFloat())
    if (mirror) matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
