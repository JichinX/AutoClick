package com.example.autoclick.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_operator_log")
data class OperatorLog(
    val rawX: Int,
    val rawY: Int,
    val viewX: Int,
    val viewY: Int,
    val optType: Int,
    val optDes: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val delayMs: Long = 200,
    /**
     * 1 true
     * -1 false
     */
    var isTemp: Int = -1
)