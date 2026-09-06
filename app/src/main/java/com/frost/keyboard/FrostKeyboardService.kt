package com.frost.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo

class FrostKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: FrostKeyboardView
    private lateinit var keyboard: Keyboard
    private var capsLock = false

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as FrostKeyboardView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        keyboardView.setDarkMode(isDark)

        keyboardView.post {
            val bmp = WallpaperBlurHelper.getBlurredWallpaper(
                this, keyboardView.width, keyboardView.height
            )
            keyboardView.setBackgroundBitmap(bmp)
        }
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardView.invalidate()
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> ic.deleteSurroundingText(1, 0)
            Keyboard.KEYCODE_SHIFT -> {
                capsLock = !capsLock
                keyboard.isShifted = capsLock
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            Keyboard.KEYCODE_MODE_CHANGE -> {
                // Hook for a symbols/number layout swap; extend as needed.
            }
            32 -> ic.commitText(" ", 1)
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && (capsLock || keyboard.isShifted)) {
                    code = Character.toUpperCase(code)
                }
                ic.commitText(code.toString(), 1)
                if (!capsLock && keyboard.isShifted) {
                    keyboard.isShifted = false
                    keyboardView.invalidateAllKeys()
                }
            }
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
