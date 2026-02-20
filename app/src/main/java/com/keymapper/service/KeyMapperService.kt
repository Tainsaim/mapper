package com.keymapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.keymapper.data.MappingRepository

class KeyMapperService : AccessibilityService() {

    companion object {
        var instance: KeyMapperService? = null
        val isRunning get() = instance != null
    }

    private lateinit var repo: MappingRepository

    override fun onServiceConnected() {
        instance = this
        repo = MappingRepository(this)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Обрабатываем только нажатие, не отпускание
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val mapping = repo.getAll().find {
            it.keyCode == event.keyCode && it.isEnabled
        } ?: return false

        performTap(mapping.tapX, mapping.tapY)
        // true = поглощаем клавишу (символ не будет напечатан)
        // false = пропускаем дальше (символ напечатается И будет тап)
        return true
    }

    private fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Не используется
    }

    override fun onInterrupt() {
        // Не используется
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
