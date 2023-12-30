package com.thk.im.android.core.db.internal

import android.app.Application
import androidx.room.Room
import com.thk.im.android.core.db.IMContactDao
import com.thk.im.android.core.db.IMDataBase
import com.thk.im.android.core.db.IMGroupDao
import com.thk.im.android.core.db.IMMessageDao
import com.thk.im.android.core.db.IMSessionDao
import com.thk.im.android.core.db.IMUserDao
import com.thk.im.android.core.db.internal.dao.IMContactDaoImp
import com.thk.im.android.core.db.internal.dao.IMGroupDaoImp
import com.thk.im.android.core.db.internal.dao.IMMessageDaoImp
import com.thk.im.android.core.db.internal.dao.IMSessionDaoImp
import com.thk.im.android.core.db.internal.dao.IMUserDaoImp
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class DefaultIMDataBase(
    private val app: Application,
    private val uId: Long,
    private val debug: Boolean = false
) : IMDataBase {

    private val roomDataBase: IMRoomDataBase
    private val prefix = "im"
    private val userDao: IMUserDao
    private val messageDao: IMMessageDao
    private val sessionDao: IMSessionDao
    private val contactDao: IMContactDao
    private val groupDao: IMGroupDao

    init {
        val dbname = if (debug) {
            "${prefix}_${uId}_debug"
        } else {
            "${prefix}_$uId"
        }
        roomDataBase = Room.databaseBuilder(app, IMRoomDataBase::class.java, dbname).build()
        userDao = IMUserDaoImp(roomDataBase)
        messageDao = IMMessageDaoImp(roomDataBase)
        sessionDao = IMSessionDaoImp(roomDataBase)
        contactDao = IMContactDaoImp(roomDataBase)
        groupDao = IMGroupDaoImp(roomDataBase)
    }

    override fun open() {
        roomDataBase.messageDao().resetSendingMsg()
    }

    override fun close() {
        roomDataBase.close()
    }

    override fun userDao(): IMUserDao {
        return userDao
    }

    override fun messageDao(): IMMessageDao {
        return messageDao
    }

    override fun sessionDao(): IMSessionDao {
        return sessionDao
    }

    override fun contactDao(): IMContactDao {
        return contactDao
    }


}