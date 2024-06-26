package com.thk.im.android.core.db.internal

import android.app.Application
import androidx.room.Room
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.db.IMContactDao
import com.thk.im.android.core.db.IMDataBase
import com.thk.im.android.core.db.IMGroupDao
import com.thk.im.android.core.db.IMMessageDao
import com.thk.im.android.core.db.IMSessionDao
import com.thk.im.android.core.db.IMSessionMemberDao
import com.thk.im.android.core.db.IMUserDao
import com.thk.im.android.core.db.internal.dao.IMContactDaoImp
import com.thk.im.android.core.db.internal.dao.IMGroupDaoImp
import com.thk.im.android.core.db.internal.dao.IMMessageDaoImp
import com.thk.im.android.core.db.internal.dao.IMSessionDaoImp
import com.thk.im.android.core.db.internal.dao.IMSessionMemberDaoImp
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
    private val sessionMemberDao: IMSessionMemberDao

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
        sessionMemberDao = IMSessionMemberDaoImp(roomDataBase)
        contactDao = IMContactDaoImp(roomDataBase)
        groupDao = IMGroupDaoImp(roomDataBase)
    }

    override fun open() {
        val sendingMessage = messageDao.findSendingMessages()
        if (sendingMessage.isNotEmpty()) {
            messageDao.resetSendingMessage()
            for (msg in sendingMessage) {
                msg.sendStatus = MsgSendStatus.SendFailed.value
                IMCoreManager.messageModule.processSessionByMessage(msg)
            }
        }
    }

    override fun close() {
        if (roomDataBase.isOpen) {
            roomDataBase.close()
        }
    }

    override fun userDao(): IMUserDao {
        return userDao
    }

    override fun messageDao(): IMMessageDao {
        return messageDao
    }

    override fun sessionMemberDao(): IMSessionMemberDao {
        return sessionMemberDao
    }

    override fun sessionDao(): IMSessionDao {
        return sessionDao
    }

    override fun contactDao(): IMContactDao {
        return contactDao
    }

    override fun groupDao(): IMGroupDao {
        return groupDao
    }


}