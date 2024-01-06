package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Group


interface IMGroupDao {

    fun insertOrReplace(groups: List<Group>)

    fun insertOrIgnore(groups: List<Group>)

    fun deleteByIds(ids: Set<Long>)

    fun findAll(): List<Group>

    fun findById(id: Long): Group?
}