package com.thk.im.android.core.db.internal.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thk.im.android.core.db.entity.Contact
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.db.internal.room.dao.ContactDao
import com.thk.im.android.core.db.internal.room.dao.GroupDao
import com.thk.im.android.core.db.internal.room.dao.MessageDao
import com.thk.im.android.core.db.internal.room.dao.SessionDao
import com.thk.im.android.core.db.internal.room.dao.SessionMemberDao
import com.thk.im.android.core.db.internal.room.dao.UserDao

@Database(
    entities = [
        Session::class,
        Message::class,
        User::class,
        Contact::class,
        Group::class,
        SessionMember::class,
    ],
    version = 1
)

internal abstract class IMRoomDataBase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun groupDao(): GroupDao
    abstract fun sessionMemberDao(): SessionMemberDao
}