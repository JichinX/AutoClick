package com.example.autoclick

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.autoclick.databinding.ActivityMainBinding
import com.example.autoclick.opt.OptEditActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val floatWindowLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkFloatWindow()
        }


    private val accessibilityLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkAccessibility()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvOpenAccessibility.setOnClickListener {
            openAccessibility()
        }
        binding.tvOpenOptEdit.setOnClickListener {
            startActivity(Intent(baseContext, OptEditActivity::class.java))
        }
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             binding.tvOpenFloatWindow.setOnClickListener {
                 opFloatWindow()
             }
         }*/
        checkAccessibility()
        /* checkFloatWindow()*/
    }

    /**
     *显示浮窗
     */
    private fun showFloatWindow() {
        startService(Intent(this, FloatingService::class.java))
    }

    private fun checkAccessibility() {
        //判断服务是否开启
        if (AccessibilityHelper.get(this)
                .checkAccessibilityEnabled("com.example.autoclick/.services.AutoClickAccessibilityService")
        ) {
            binding.tvOpenAccessibility.visibility = View.GONE
        }
    }

    private fun checkFloatWindow() {
        //判断浮窗权限
        binding.tvOpenFloatWindow.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                View.VISIBLE
            } else {
                showFloatWindow()
                View.GONE
            }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun opFloatWindow() {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
        intent.data = Uri.parse("package:$packageName")
        floatWindowLaunch.launch(intent)
    }

    private fun openAccessibility() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        accessibilityLaunch.launch(intent)
    }
}