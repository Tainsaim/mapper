package com.keymapper.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.keymapper.data.KeyMapping
import com.keymapper.data.MappingRepository
import com.keymapper.databinding.ActivityMainBinding
import com.keymapper.service.KeyMapperService
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: MappingRepository
    private lateinit var adapter: MappingAdapter

    private enum class Step { IDLE, WAITING_KEY, WAITING_TAP }

    private var step = Step.IDLE
    private var capturedKeyCode: Int? = null
    private var capturedKeyLabel: String? = null
    private var overlayView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = MappingRepository(this)

        adapter = MappingAdapter(
            items = repo.getAll(),
            onToggle = { id ->
                repo.toggle(id)
                refreshList()
            },
            onDelete = { id ->
                repo.delete(id)
                refreshList()
            }
        )

        binding.rvMappings.layoutManager = LinearLayoutManager(this)
        binding.rvMappings.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        binding.rvMappings.adapter = adapter

        binding.btnService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        binding.btnAdd.setOnClickListener {
            startAddFlow()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val serviceOk = KeyMapperService.isRunning
        val overlayOk = Settings.canDrawOverlays(this)

        binding.tvServiceStatus.text = buildString {
            append(if (serviceOk) "✅ Сервис активен" else "❌ Сервис выключен")
            append("    ")
            append(if (overlayOk) "✅ Оверлей OK" else "❌ Оверлей запрещён")
        }
        binding.tvServiceStatus.setTextColor(
            if (serviceOk && overlayOk) Color.parseColor("#2E7D32")
            else Color.parseColor("#C62828")
        )

        binding.btnAdd.isEnabled = serviceOk && overlayOk
    }

    // ────── Поток добавления маппинга ──────

    private fun startAddFlow() {
        step = Step.WAITING_KEY
        capturedKeyCode = null
        capturedKeyLabel = null
        binding.tvHint.text = "⌨️ Нажмите клавишу на клавиатуре..."
        binding.tvHint.setTextColor(Color.parseColor("#1565C0"))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (step == Step.WAITING_KEY) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                cancelFlow()
                return true
            }
            capturedKeyCode = keyCode
            capturedKeyLabel = KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
            step = Step.WAITING_TAP
            binding.tvHint.text = "✅ Клавиша «$capturedKeyLabel» поймана!\n👆 Теперь тапните по синему оверлею в нужное место экрана"
            showTapPickerOverlay()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showTapPickerOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Нет разрешения на оверлей!", Toast.LENGTH_SHORT).show()
            cancelFlow()
            return
        }

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = View(this).apply {
            setBackgroundColor(Color.argb(100, 33, 150, 243))
            setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN && step == Step.WAITING_TAP) {
                    onTapPicked(motionEvent.rawX, motionEvent.rawY)
                }
                true
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        wm.addView(overlayView, params)
    }

    private fun onTapPicked(x: Float, y: Float) {
        removeOverlay()

        val keyCode = capturedKeyCode ?: return
        val keyLabel = capturedKeyLabel ?: return

        val mapping = KeyMapping(
            id = UUID.randomUUID().toString(),
            keyCode = keyCode,
            keyLabel = keyLabel,
            tapX = x,
            tapY = y
        )

        repo.add(mapping)
        refreshList()
        cancelFlow()
        Toast.makeText(this, "✅ Маппинг добавлен: $keyLabel → (${x.toInt()}, ${y.toInt()})", Toast.LENGTH_LONG).show()
    }

    private fun cancelFlow() {
        step = Step.IDLE
        binding.tvHint.text = ""
        capturedKeyCode = null
        capturedKeyLabel = null
        removeOverlay()
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            } catch (_: Exception) {
            }
            overlayView = null
        }
    }

    private fun refreshList() {
        adapter.update(repo.getAll())
    }

    override fun onStop() {
        super.onStop()
        if (step != Step.IDLE) {
            removeOverlay()
        }
    }
}
