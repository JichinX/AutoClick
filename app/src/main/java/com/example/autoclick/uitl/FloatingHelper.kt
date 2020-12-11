package com.example.autoclick.uitl

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.autoclick.tag
import kotlin.math.roundToInt

class FloatingOnTouchListener(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams,
    private val updateView: View? = null
) : View.OnTouchListener {
    private val start = arrayOf(0, 0)
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        Log.i(tag(), "onTouch: $v - $event")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                start[0] = event.rawX.roundToInt()
                start[1] = event.rawY.roundToInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = event.rawX.roundToInt()
                val nowY = event.rawY.roundToInt()
                val moveX = nowX - start[0]
                val moveY = nowY - start[1]
                start[0] = nowX
                start[1] = nowY

                layoutParams.x += moveX
                layoutParams.y += moveY
                val view = updateView ?: v
                windowManager.updateViewLayout(view, layoutParams)
            }
            else -> {
            }
        }
        return false
    }
}