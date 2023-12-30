package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Group


interface IMGroupDao {

    fun insertOrUpdateGroups(groups: List<Group>)

    fun insertOrIgnoreGroups(groups: List<Group>)

    fun queryAllGroups() : List<Group>
}