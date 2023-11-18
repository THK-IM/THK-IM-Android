package com.thk.im.android.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thk.im.android.core.db.dao.ContactorApplyDao
import com.thk.im.android.core.db.dao.ContactorApplyMsgDao
import com.thk.im.android.core.db.dao.ContactorDao
import com.thk.im.android.core.db.dao.GroupApplyDao
import com.thk.im.android.core.db.dao.GroupApplyMsgDao
import com.thk.im.android.core.db.dao.GroupDao
import com.thk.im.android.core.db.dao.GroupMemberDao
import com.thk.im.android.core.db.dao.MessageDao
import com.thk.im.android.core.db.dao.SessionDao
import com.thk.im.android.core.db.dao.UserDao
import com.thk.im.android.core.db.entity.Contactor
import com.thk.im.android.core.db.entity.ContactorApply
import com.thk.im.android.core.db.entity.ContactorApplyMsg
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.GroupApply
import com.thk.im.android.core.db.entity.GroupApplyMsg
import com.thk.im.android.core.db.entity.GroupMember
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User

@Database(
    entities = [
        Session::class,
        Message::class,
        User::class,
        Contactor::class,
        Group::class,
        GroupMember::class,
        ContactorApply::class,
        ContactorApplyMsg::class,
        GroupApply::class,
        GroupApplyMsg::class,
    ],
    version = 1
)
abstract class DataBase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun groupApplyDao(): GroupApplyDao
    abstract fun groupApplyMsgDao(): GroupApplyMsgDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun contactorDao(): ContactorDao
    abstract fun contactorApplyDao(): ContactorApplyDao
    abstract fun contactorApplyMsgDao(): ContactorApplyMsgDao
    abstract fun messageDao(): MessageDao
}