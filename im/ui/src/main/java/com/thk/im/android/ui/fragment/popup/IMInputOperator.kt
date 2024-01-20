package com.thk.im.android.ui.fragment.popup

import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User

interface IMInputOperator {

    fun insertAtSessionMember(sessionMember: SessionMember, user: User)

}