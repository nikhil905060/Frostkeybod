package com.frost.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

class FrostKeyboardView(context: Context, attrs: AttributeSet?) : KeyboardView(context, attrs) {

    private var blurredBackground: Bitmap? = null

    private val scrimPaint = Paint().apply {
        color = Color.parseColor("#55FFFFFF")
    }

    private val fallbackPaint = Paint().apply {
        color = Color.parseColor("#CCF5F5F7")
    }

    fun setBackgroundBitmap(bmp: Bitmap?) {
        blurredBackground = bmp
        invalidate()
    }

    fun setDarkMode(isDark: Boolean) {
        scrimPaint.color = if (isDark) Color.parseColor("#66000000") else Color.parseColor("#55FFFFFF")
        fallbackPaint.color = if (isDark) Color.parseColor("#CC1C1C1E") else Color.parseColor("#CCF5F5F7")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val bmp = blurredBackground
        if (bmp != null && width > 0 && height > 0) {
            val src = Rect(0, 0, bmp.width, bmp.height)
            val dst = Rect(0, 0, width, height)
            canvas.drawBitmap(bmp, src, dst, null)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        } else {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fallbackPaint)
        }
        super.onDraw(canvas)
    }
}
