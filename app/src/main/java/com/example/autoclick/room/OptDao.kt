package com.example.autoclick.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.autoclick.room.entity.OperatorLog

@Dao
interface OptDao {
    @Insert
    fun addOpt(operatorLog: OperatorLog)

    @Query("SELECT * FROM tbl_operator_log ORDER BY id DESC")
    fun getAll(): LiveData<List<OperatorLog>>

    @Query("SELECT * FROM tbl_operator_log  ORDER BY id DESC LIMIT 1")
    fun getLatest(): LiveData<OperatorLog>

    @Query("SELECT * FROM tbl_operator_log WHERE isTemp=1")
    fun getTempOptList(): LiveData<List<OperatorLog>>

    @Insert
    fun addOptList(list: List<OperatorLog>)
}
