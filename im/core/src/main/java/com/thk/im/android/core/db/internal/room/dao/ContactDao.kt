package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thk.im.android.core.db.entity.Contact

@Dao
internal interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceContacts(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreContacts(contacts: List<Contact>)

    @Query("select * from contact")
    fun queryAllContacts(): List<Contact>

}