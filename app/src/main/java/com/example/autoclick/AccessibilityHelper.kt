package com.example.autoclick

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityManager

class AccessibilityHelper private constructor(context: Context) {
    private val accessibilityManager: AccessibilityManager

    init {
        accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    /**
     * 检测服务是否开启
     */
    fun checkAccessibilityEnabled(serviceName: String): Boolean {
        val enabledList =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledList.any { accessibilityServiceInfo ->
            Log.i(tag(), "checkAccessibilityEnabled: ${accessibilityServiceInfo.id}")
            accessibilityServiceInfo.id == serviceName
        }
    }

    companion object {
        private var accessibilityManager: AccessibilityHelper? = null
        fun get(context: Context): AccessibilityHelper {
            if (null == accessibilityManager) {
                accessibilityManager = AccessibilityHelper(context)
            }
            return accessibilityManager!!
        }
    }
}