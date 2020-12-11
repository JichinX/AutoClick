package com.example.autoclick.services

import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.autoclick.*
import com.example.autoclick.opt.OptEditActivity
import com.example.autoclick.room.OptDatabase
import com.example.autoclick.room.entity.OperatorLog
import com.example.autoclick.uitl.FloatingOnTouchListener
import com.example.autoclick.uitl.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt


@RequiresApi(Build.VERSION_CODES.N)
class AutoClickAccessibilityService : LifecycleAccessibilityService(), LifecycleEventObserver {
    private val centerOffset by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
            .roundToInt()
    }
    private lateinit var detector: GestureDetector
    private val optRecordList = mutableListOf<OperatorLog>()
    private val optViewList = mutableListOf<View>()
    private val newOptLiveData = MutableLiveData<OperatorLog>()
    private lateinit var optRecordWindow: FrameLayout
    private lateinit var optWindow: FrameLayout
    private val optExisting = AtomicBoolean(false)
    private val wm by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }
    private val handler by lazy {
        Handler()
    }
    private val gestureDescription by lazy {
        val path = Path().apply {
            moveTo(540F, 1800F)
            lineTo(540F, 200F)
        }
        GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 20, 500))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(tag(), "onCreate: ")
        /*OptDatabase.get(this).optDao().getLatest().observe(this) {
            Log.i(tag(), "latest Operate: $it")
            performClick(it)
        }*/
        newOptLiveData.observe(this) {
            Log.i(tag(), "latest Operate: $it")
            performClick(it)
        }
    }

    /**
     *响应点击事件
     */
    private fun performClick(log: OperatorLog?) {
        Log.i(tag(), "performClick: $log")
        log?.apply {
            optRecordList.add(log)
            addOptView(log)
            lifecycleScope.launch {
                closeRecordWindow()
                delay(200L)
                doClick(log)
                delay(200L)
                openOptRecordWindow()
            }
        }
    }

    private fun addOptView(log: OperatorLog) {
        val optView = FrameLayout(this)
        LayoutInflater.from(this).inflate(R.layout.float_window_opt_step, optView)
//        configOptView(optView, log)
        wm.addView(optView, createOptViewLp(log))
        optViewList.add(optView)
    }

    private fun configOptView(optView: FrameLayout, log: OperatorLog) {
        optView.findViewById<TextView>(R.id.tv_log_step).text = log.id.toString()
    }

    private fun createOptViewLp(operatorLog: OperatorLog): WindowManager.LayoutParams =
        WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            gravity = Gravity.START or Gravity.TOP
            x = operatorLog.viewX - centerOffset
            y = operatorLog.viewY - centerOffset
            format = PixelFormat.TRANSLUCENT
            flags =
                flags or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

    private fun openOptRecordWindow() {
        initRecordWindow()
    }

    private fun doClick(log: OperatorLog) {
        Log.i(tag(), "doClick: $log")
        val path = Path().apply {
            moveTo(log.rawX.toFloat(), log.rawY.toFloat())
        }
        val description = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            .build()
        val dispatchGesture =
            dispatchGesture(description, null, null)
        Log.i(tag(), "doClick: dispatchGesture -> $dispatchGesture")
    }

    /**
     * 发生用户界面事件
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i(tag(), "onAccessibilityEvent: $event")
        when (event?.eventType) {
            TYPE_VIEW_CLICKED -> {
                Log.i(tag(), "onAccessibilityEvent: TYPE_VIEW_CLICKED -> ${event.source}")
            }
            TYPE_VIEW_SCROLLED -> {
                Log.i(tag(), "onAccessibilityEvent: TYPE_VIEW_SCROLLED -> ${event.source}")
            }
            else -> {
            }
        }
    }

    private fun startOpt() {
        if (optExisting.get()) {
            return
        }
        handler.postDelayed({
            val tvDigital = findTextView("数码")
            tvDigital
                .firstOrNull {
                    it.className == "android.widget.TextView"
                }?.run {
                    performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    optExisting.set(true)
                }

        }, 1000L)
    }

    private fun findTextView(text: CharSequence): List<AccessibilityNodeInfo> {
        val list = arrayListOf<AccessibilityNodeInfo>()
        lisChild(rootInActiveWindow, list) {
            it.text == text
        }
        Log.i(tag(), "findTextView with text: $text  result:$list")
        return list
    }

    private fun startAutoOpt() {
        handler.postDelayed({
            dispatchGesture(gestureDescription, object : GestureResultCallback() {

            }, null)
        }, 1000L)
    }

    private fun lisChild(
        child: AccessibilityNodeInfo,
        list: MutableList<AccessibilityNodeInfo>,
        filter: (AccessibilityNodeInfo) -> Boolean
    ) {
        if (filter(child)) {
            list.add(child)
        }
        if (child.childCount > 0) {
            for (index in 0 until child.childCount) {
                child.getChild(index)?.apply {
                    lisChild(this, list, filter)
                }
            }
        }
    }

    override fun onInterrupt() {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(tag(), "onServiceConnected: ")
        openOptWindow()
    }

    private fun initFloatOptWindow() {
        val mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags =
                flags or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.float_window_opt, mLayout)
        configOpt(mLayout, lp)
        wm.addView(mLayout, lp)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configOpt(container: FrameLayout, lp: WindowManager.LayoutParams) {
        optWindow = container
        container.findViewById<TextView>(R.id.tv_float_opt_close_opt).setOnClickListener {
            //关闭
            toast("close")
            closeOptWindow()
        }
        container.findViewById<TextView>(R.id.tv_float_opt_start_record).setOnClickListener {
            //开始录制
            toast("开始录制")
            startOptRecord()
        }
        container.findViewById<TextView>(R.id.tv_float_opt_clear).setOnClickListener {
            //开始录制
            toast("清空记录")
            clearOptView()
        }
        container.findViewById<TextView>(R.id.tv_float_opt_record_play).setOnClickListener {
            //测试点击
            toast("播放脚本")
            playOptRecords()
        }
        container.findViewById<TextView>(R.id.tv_float_opt_edit).setOnClickListener {
            //开始录制
            toast("编辑脚本")
            lifecycleScope.launch(Dispatchers.Main) {
                saveTemp()
                startActivity(
                    Intent(baseContext, OptEditActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                )
            }
        }
        container.setOnTouchListener(FloatingOnTouchListener(wm, lp /*container*/))
    }

    private suspend fun saveTemp() = withContext(Dispatchers.IO) {
        val list = optRecordList.toList()
        list.forEach {
            it.isTemp = 1
        }
        OptDatabase.get(applicationContext).optDao().addOptList(list)
    }

    private fun playOptRecords() {
        lifecycleScope.launch {
            wm.removeView(optWindow)
            for (index in 0 until optRecordList.size) {
                val opt = optRecordList[index]
                delay(200L)
                doClick(opt)
                delay(opt.delayMs)
            }
            openOptWindow()
        }
    }

    private fun clearOptView() {
        optRecordList.clear()
        optViewList.forEach {
            wm.removeView(it)
        }
        optViewList.clear()
    }

    private fun closeOptWindow() {
        clearOptView()
        wm.removeView(optWindow)
    }

    /**
     *开始录制
     */
    private fun startOptRecord() {
        wm.removeView(optWindow)
        initRecordWindow()
    }

    private fun initRecordWindow() {
        val mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags =
                flags or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_FULLSCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.float_window_record, mLayout)
        configOptRecord(mLayout, lp)
        wm.addView(mLayout, lp)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configOptRecord(container: FrameLayout, lp: WindowManager.LayoutParams) {
        optRecordWindow = container
        container.findViewById<ImageView>(R.id.iv_float_bg_close).setOnClickListener {
            closeRecordWindow()
            openOptWindow()
        }
        container.setOnTouchListener { _, event ->
            if (!::detector.isInitialized) {
                val gestureRecord =
                    GestureRecordListener(newOptLiveData)
                detector = GestureDetector(applicationContext, gestureRecord)
            }
            detector.onTouchEvent(event)
        }
    }

    private fun openOptWindow() {
        initFloatOptWindow()
    }

    private fun closeRecordWindow() {
        wm.removeView(optRecordWindow)
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        return root?.apply {
            val nodeDeque = ArrayDeque<AccessibilityNodeInfo>()
            nodeDeque.add(root)
            while (nodeDeque.isNotEmpty()) {
                val first = nodeDeque.removeFirst()
                if (first.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                    return first
                }
                for (index in 0 until first.childCount) {
                    nodeDeque.addLast(first.getChild(index))
                }
            }
        }
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.i(tag(), "onStateChanged: $event")
    }

}