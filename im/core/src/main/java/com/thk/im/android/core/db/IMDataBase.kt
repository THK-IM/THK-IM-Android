package com.thk.im.android.core.db


interface IMDataBase {
    fun open()

    fun close()

    fun userDao(): IMUserDao

    fun messageDao(): IMMessageDao

    fun sessionMemberDao(): IMSessionMemberDao

    fun sessionDao(): IMSessionDao

    fun contactDao(): IMContactDao

    fun groupDao(): IMGroupDao
}