package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Group


interface IMGroupDao {

    fun insertOrReplaceGroups(groups: List<Group>)

    fun insertOrIgnoreGroups(groups: List<Group>)

    fun deleteGroupByIds(ids: Set<Long>)

    fun queryAllGroups(): List<Group>

    fun findOne(id: Long): Group?
}