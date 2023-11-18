package com.thk.im.android.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thk.im.android.core.db.entity.Contactor

@Dao
interface ContactorDao {

    /**
     * 插入联系人到数据库
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactor(vararg contactor: Contactor)

    /**
     * 查找联系人
     */
    @Query("select * from contactor where uid = :id")
    fun findContactor(id: Long): Contactor

    @Update
    fun updateContactor(contactor: Contactor): Int
}