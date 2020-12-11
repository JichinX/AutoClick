package com.example.autoclick

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import com.example.autoclick.room.entity.OperatorLog
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class GestureRecordListener(
    private val newOptLiveData: MutableLiveData<OperatorLog>,
    private val canSave: AtomicBoolean = AtomicBoolean(true)
) :
    GestureDetector.SimpleOnGestureListener() {
    private var downEventTmp: MotionEvent? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun onDown(e: MotionEvent): Boolean {
        Log.i(tag(), "onDown: ${e.x} , ${e.y}")
        downEventTmp = e
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return e?.let { motionEvent ->
            Log.i(tag(), "onSingleTapConfirmed: (${e.x},${e.y}) (${e.rawX},${e.rawY})")
            newOptLiveData.value =
                OperatorLog(
                    (motionEvent.rawX).roundToInt(),
                    (motionEvent.rawY).roundToInt(),
                    (motionEvent.x).roundToInt(),
                    (motionEvent.y).roundToInt(),
                    Const.OPT_TYPE_SINGLE_TAP,
                    "点击"
                )
            true
        } ?: false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.i(tag(), "onDoubleTap: ")
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
//        Log.i(tag(), "onScroll: ")
//        Log.i(tag(), "downEvent ${e1 === downEventTmp}")
//        Log.i(tag(), "onScroll: e1-> $e1")
//        Log.i(tag(), "onScroll: e2-> $e2")
        Log.i(tag(), "onScroll: distance-> distanceX: $distanceX , distanceY: $distanceY")
        return true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i(tag(), "onSingleTapUp: ")
        return true
    }

}
