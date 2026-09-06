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
    private val density = resources.displayMetrics.density

    private val scrimPaint = Paint().apply {
        color = Color.parseColor("#D9FFFFFF")
    }

    private val fallbackPaint = Paint().apply {
        color = Color.parseColor("#F0F5F5F7")
    }

    private val normalKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFFFF")
    }

    private val functionKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFADADB0")
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33000000")
    }

    fun setBackgroundBitmap(bmp: Bitmap?) {
        blurredBackground = bmp
        invalidate()
    }

    fun setDarkMode(isDark: Boolean) {
        scrimPaint.color = if (isDark) Color.parseColor("#D9000000") else Color.parseColor("#D9FFFFFF")
        fallbackPaint.color = if (isDark) Color.parseColor("#F01C1C1E") else Color.parseColor("#F0F5F5F7")
        normalKeyPaint.color = if (isDark) Color.parseColor("#FF3A3A3C") else Color.parseColor("#FFFFFFFF")
        functionKeyPaint.color = if (isDark) Color.parseColor("#FF232324") else Color.parseColor("#FFADADB0")
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
        drawKeyCaps(canvas)
        super.onDraw(canvas)
    }

    private fun drawKeyCaps(canvas: Canvas) {
        val kb = keyboard ?: return
        val margin = 2f * density
        val radius = 6f * density
        val shadowOffset = 1.5f * density
        for (key in kb.keys) {
            val code = key.codes.getOrNull(0) ?: 0
            val isFunction = code < 0
            val paint = if (isFunction) functionKeyPaint else normalKeyPaint
            val left = key.x.toFloat() + margin
            val top = key.y.toFloat() + margin
            val right = key.x.toFloat() + key.width - margin
            val bottom = key.y.toFloat() + key.height - margin
            canvas.drawRoundRect(left, top + shadowOffset, right, bottom + shadowOffset, radius, radius, shadowPaint)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }
    }
}
