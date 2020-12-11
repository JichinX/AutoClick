package com.example.autoclick

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleService
import com.example.autoclick.room.OptDatabase
import com.example.autoclick.room.entity.OperatorLog
import com.example.autoclick.uitl.FloatingOnTouchListener
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt


class FloatingService : LifecycleService() {
    private val centerOffset by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24F, resources.displayMetrics)
            .roundToInt()
    }
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var detector: GestureDetector
    private val canSave = AtomicBoolean(false)
    private lateinit var optLp: WindowManager.LayoutParams
    private lateinit var optView: View
    private lateinit var bgLp: WindowManager.LayoutParams
    private lateinit var windowManager: WindowManager
    private lateinit var bgView: View
    private val optLogList = mutableListOf<OperatorLog>()
    private val logViews = mutableListOf<View>()
    override fun onCreate() {
        super.onCreate()
        OptDatabase.get(this).optDao().getAll().observe(this) { newList ->
            Log.i(tag(), "new: $newList")
            Log.i(tag(), "old: $optLogList")
            newList.filterNot {
                optLogList.contains(it)
            }.also { list ->
                optLogList.addAll(list)
                addOperator(list)
            }
        }
        OptDatabase.get(this).optDao().getLatest().observe(this) {
            Log.i(tag(), "latest Operate: $it")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return OptBinder(this)
    }

    private fun addOperator(addList: List<OperatorLog>) {
        addList.forEach { log ->
            val view = createOperatorView(log.id.toString())
            val lp = createOperatorLp()
            lp.x = log.viewX - centerOffset
            lp.y = log.viewY - centerOffset
            if (::windowManager.isInitialized) {
                windowManager.addView(view, lp)
                logViews.add(view)
            }
        }
    }

    private fun createOperatorView(text: String): View =
        LayoutInflater.from(this).inflate(R.layout.float_window_opt_step, null).also { logView ->
            logView.findViewById<TextView>(R.id.tv_log_step).text = text
        }

    private fun createOperatorLp(): WindowManager.LayoutParams =
        WindowManager.LayoutParams().also { layoutParams ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            layoutParams.gravity = Gravity.START or Gravity.TOP
            layoutParams.format = PixelFormat.RGBA_8888
            layoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //宽高自适应
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showFloatWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            // 获取WindowManager服务
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            bgView = createBgView()
            bgLp = createBgLp()
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(bgView, bgLp)
            optView = createOptView()
            optLp = createOptLp()
            optView.setOnTouchListener(FloatingOnTouchListener(windowManager, optLp))
            //操作面板
            windowManager.addView(optView, optLp)
        }
    }

    private fun createOptLp(): WindowManager.LayoutParams =
        WindowManager.LayoutParams().also { layoutParams ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            layoutParams.x = 200
            layoutParams.y = 200
            layoutParams.format = PixelFormat.RGBA_8888
            layoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //宽高自适应
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }

    private fun createOptView(): View {
        return LayoutInflater.from(this).inflate(R.layout.float_window_opt, null).also { opt ->
            opt.findViewById<TextView>(R.id.tv_float_opt_close_opt).setOnClickListener {
                closeService()
            }
            opt.findViewById<TextView>(R.id.tv_float_opt_start_record).setOnClickListener {
                startRecord()
            }
        }
    }

    private fun startRecord() {
        canSave.set(true)
        closeOpt()
        enableBgClose(true)
    }

    private fun enableBgClose(enable: Boolean) {
        windowManager.removeView(bgView)
        bgView.findViewById<ImageView>(R.id.iv_float_bg_close).visibility =
            if (enable) View.VISIBLE else View.GONE
        windowManager.addView(bgView, createBgLp())
    }

    private fun closeOpt() {
        if (::windowManager.isInitialized) {
            windowManager.removeView(optView)
        }
    }

    private fun closeService() {
        stopSelf()
    }

    private fun createBgLp(): WindowManager.LayoutParams =
        WindowManager.LayoutParams().also { layoutParams ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            layoutParams.format = PixelFormat.RGBA_8888
            if (canSave.get()) {
                //需要记录点击的位置等信息
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                //不需要点击事件，将点击事件偷穿到后面的窗口
                layoutParams.flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            //宽高自适应
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        }

    private fun createBgView(): View =
        LayoutInflater.from(this).inflate(R.layout.float_window_bg, null).also { bg ->
            bg.findViewById<ImageView>(R.id.iv_float_bg_close)
                .setOnClickListener {
                    stopRecord()
                }
            /*bg.setOnTouchListener { _, event ->
                if (!::detector.isInitialized) {
                    val gestureRecord =
                        GestureRecordListener(OptDatabase.get(applicationContext), canSave)
                    detector = GestureDetector(applicationContext, gestureRecord)
                }
                detector.onTouchEvent(event)
            }*/
        }

    private fun stopRecord() {
        canSave.set(false)
        enableBgClose(false)
        openOpt()
        executor.submit {
            OptDatabase.get(this).clearAllTables()
        }
        clearLogs()
    }

    private fun clearLogs() {
        logViews.forEach {
            windowManager.removeView(it)
        }
        logViews.clear()
    }

    private fun openOpt() {
        windowManager.addView(optView, optLp)
    }

    override fun onDestroy() {
        Log.i(tag(), "onDestroy: ")
        closeAllView()
        super.onDestroy()
    }

    private fun closeAllView() {
        closeOpt()
        closeBg()
    }

    private fun closeBg() {
        if (::windowManager.isInitialized) {
            windowManager.removeView(bgView)
        }
    }

    fun pauseRecord() {
        Log.i(tag(), "pauseRecord: ")
        canSave.set(false)
        windowManager.removeView(bgView)
        windowManager.addView(bgView, createBgLp())
    }

    fun resumeRecord() {
        Log.i(tag(), "resumeRecord: ")
        canSave.set(true)
        windowManager.removeView(bgView)
        windowManager.addView(bgView, createBgLp())
    }
}

class OptBinder(private val service: FloatingService) : Binder() {

    fun resumeRecord() {
        service.resumeRecord()
    }

    fun pauseRecord() {
        service.pauseRecord()
    }
}
