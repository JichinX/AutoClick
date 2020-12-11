package com.example.autoclick.uitl

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import java.lang.StringBuilder

fun Context.toast(vararg msgs: String) {
    val msg = StringBuilder().apply {
        msgs.forEach {
            append(it).append(" ")
        }
    }.toString()
    Toast.makeText(applicationContext, msg, LENGTH_SHORT).show()
}