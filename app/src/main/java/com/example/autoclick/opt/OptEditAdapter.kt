package com.example.autoclick.opt

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.example.autoclick.R
import com.example.autoclick.databinding.ItemOptEditBinding
import com.example.autoclick.room.entity.OperatorLog
import com.example.autoclick.tag

class OptEditAdapter(private val list: List<OperatorLog>) : RecyclerView.Adapter<OptViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptViewHolder =
        OptViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_opt_edit, null).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            })

    override fun onBindViewHolder(holder: OptViewHolder, position: Int) =
        holder.bindData(list[position])

    override fun getItemCount(): Int = list.size
}

class OptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = ItemOptEditBinding.bind(itemView)

    fun bindData(operatorLog: OperatorLog) {
        Log.i(tag(), "bindData: $operatorLog")
        binding.ivOptEdit.setOnClickListener {

        }
        binding.ivOptTrash.setOnClickListener {

        }
        binding.tvOptDes.text =
            "${operatorLog.id}.${operatorLog.optDes}(${operatorLog.rawX},${operatorLog.rawY})"
        binding.tvOptDelay.text = "${operatorLog.delayMs}ms"
    }
}
