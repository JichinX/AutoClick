package com.example.autoclick.opt

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autoclick.databinding.ActivityOptEditBinding
import com.example.autoclick.room.OptDatabase
import com.example.autoclick.room.entity.OperatorLog

class OptEditActivity : AppCompatActivity() {
    private val list = mutableListOf<OperatorLog>()
    private lateinit var binding: ActivityOptEditBinding
    private lateinit var optAdapter: OptEditAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //监听缓存的脚本
        OptDatabase.get(this).optDao().getTempOptList().observe(this) {
            list.clear()
            list.addAll(it)
            notifyData()
        }
        initView()
    }

    private fun initView() {
        binding.btnScriptSave.setOnClickListener {

        }
        binding.rvOptTempList.apply {
            adapter = OptEditAdapter(list)
            layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    private fun notifyData() {
        if (::optAdapter.isInitialized) {
            optAdapter.notifyDataSetChanged()
        }
    }

}
