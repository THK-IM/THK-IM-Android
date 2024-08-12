package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.Contact

@Dao
internal interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(contacts: List<Contact>)

    @Query("select * from contact")
    fun findAll(): List<Contact>

    @Query("select * from contact where id= :entityId")
    fun findByUserId(entityId: Long): Contact?

    @Query("select * from contact where (relation & :relation ) != 0")
    fun findByRelation(relation: Int): List<Contact>


    @Query("select * from contact where id in (:ids)")
    fun findByUserIds(ids: List<Long>): List<Contact>

}