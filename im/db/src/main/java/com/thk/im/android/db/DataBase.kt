package com.thk.im.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thk.im.android.db.dao.*
import com.thk.im.android.db.entity.*

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