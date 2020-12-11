package com.example.autoclick.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.autoclick.room.entity.OperatorLog

@Database(entities = [OperatorLog::class], version = 1, exportSchema = false)
abstract class OptDatabase : RoomDatabase() {
    abstract fun optDao(): OptDao

    companion object {
        private lateinit var instance: OptDatabase
        fun get(context: Context): OptDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(context, OptDatabase::class.java, "db_opt").build()
            }
            return instance
        }
    }
}