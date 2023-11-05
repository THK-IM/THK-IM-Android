package com.thk.im.android.ui.protocol.internal

import com.thk.im.android.db.entity.Session

interface IMSessionVHOperator {

    fun updateSession(session: Session)

    fun deleteSession(session: Session)

    fun openSession(session: Session)

}