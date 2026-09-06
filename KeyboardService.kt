package com.example.frostkeyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView

class KeyboardService : InputMethodService() {

    private var vibrator: Vibrator? = null
    private var popupWindow: PopupWindow? = null
    private var initialTouchX = 0f

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
    }

    // 1. Tactile Haptic Feedback on Keypress
    fun triggerKeyHaptic(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    // 2. Gboard-Style Spacebar Cursor Scrubbing
    fun setupSpacebarSwipe(spacebarView: View) {
        spacebarView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialTouchX
                    if (Math.abs(deltaX) > 25) { // Sensitivity threshold
                        val inputConnection = currentInputConnection
                        if (deltaX > 0) {
                            // Move cursor right
                            inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                        } else {
                            // Move cursor left
                            inputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                        }
                        initialTouchX = event.x
                    }
                    true
                }
                else -> false
            }
        }
    }

    // 3. Voice Input Trigger (Speech-to-text)
    fun launchVoiceTyping() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Speech recognizer fallback
        }
    }

    // 4. Quick Word Deletion (Swipe Left on Backspace)
    fun setupBackspaceGesture(backspaceView: View) {
        var startX = 0f
        backspaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    false
                }
                MotionEvent.ACTION_UP -> {
                    if (startX - event.x > 100) { // Dragged left
                        deletePreviousWord()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun deletePreviousWord() {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val lastSpaceIndex = textBefore.trimEnd().lastIndexOfAny(charArrayOf(' ', '\n'))
        val deleteCount = if (lastSpaceIndex != -1) textBefore.length - lastSpaceIndex else textBefore.length
        ic.deleteSurroundingText(deleteCount, 0)
    }
}
