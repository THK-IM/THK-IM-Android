package com.thk.im.android.core.db.internal.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thk.im.android.core.db.internal.room.dao.MessageDao
import com.thk.im.android.core.db.internal.room.dao.SessionDao
import com.thk.im.android.core.db.internal.room.dao.UserDao
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User

@Database(
    entities = [
        Session::class,
        Message::class,
        User::class,
    ],
    version = 1
)

internal abstract class IMRoomDataBase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
}