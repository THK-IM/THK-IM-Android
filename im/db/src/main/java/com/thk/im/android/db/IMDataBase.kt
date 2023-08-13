package com.thk.im.android.db

import android.app.Application
import androidx.room.Room
import com.thk.im.android.db.dao.*

class IMDataBase(val app: Application, private val uId: Long, private val debug: Boolean = false) {

    private var db: DataBase? = null
    private val prefix = "im"

    fun open() {
        if (db == null) {
            val dbname = if (debug) {
                "${prefix}_${uId}_debug"
            } else {
                "${prefix}_$uId"
            }
            db = Room.databaseBuilder(app, DataBase::class.java, dbname)
                .build()
        }
    }

    fun initData() {
        messageDao().resetSendingMsg()
    }

    fun sessionDao(): SessionDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.sessionDao()
    }

    fun userDao(): UserDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.userDao()
    }

    fun contractorDao(): ContactorDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.contactorDao()
    }

    fun contractorApplyDao(): ContactorApplyDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.contactorApplyDao()
    }

    fun contractorApplyMsgDao(): ContactorApplyMsgDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.contactorApplyMsgDao()
    }

    fun messageDao(): MessageDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.messageDao()
    }

    fun groupDao(): GroupDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.groupDao()
    }

    fun groupMemberDao(): GroupMemberDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.groupMemberDao()
    }

    fun groupApplyDao(): GroupApplyDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.groupApplyDao()
    }

    fun groupApplyMsgDao(): GroupApplyMsgDao {
        if (db == null) {
            throw Exception("db init first")
        }
        return db!!.groupApplyMsgDao()
    }

    fun close() {
        db?.close()
    }

}