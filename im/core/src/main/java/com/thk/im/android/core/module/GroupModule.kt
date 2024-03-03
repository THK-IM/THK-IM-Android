package com.thk.im.android.core.module

import com.thk.im.android.core.db.entity.Group
import io.reactivex.Flowable


interface GroupModule : BaseModule {

    fun queryServerGroup(id: Long): Flowable<Group?>

    fun findById(id: Long): Flowable<Group?>

    fun queryAllGroups(): Flowable<List<Group>>

}
