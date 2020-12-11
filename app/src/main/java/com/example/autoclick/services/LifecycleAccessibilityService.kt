package com.example.autoclick.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher

open class LifecycleAccessibilityService : AccessibilityService(), LifecycleOwner {

    private val mDispatcher = ServiceLifecycleDispatcher(this)

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onServiceConnected() {
        mDispatcher.onServicePreSuperOnStart()
        super.onServiceConnected()
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return mDispatcher.lifecycle
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }
}