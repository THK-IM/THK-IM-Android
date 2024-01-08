package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.module.GroupModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

open class DefaultGroupModule : GroupModule {
    override fun queryServerGroup(id: Long): Flowable<Group?> {
        return Flowable.just(Group(id))
    }

    override fun findById(id: Long): Flowable<Group?> {
        return Flowable.create<Group?>({
            val group = IMCoreManager.getImDataBase().groupDao().findById(id)
            if (group != null) {
                it.onNext(group)
            } else {
                it.onNext(Group(id))
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.cTime == 0L) {
                return@flatMap queryServerGroup(id).flatMap { serverGroup ->
                    IMCoreManager.db.groupDao().insertOrReplace(listOf(serverGroup))
                    Flowable.just(serverGroup)
                }
            } else {
                return@flatMap Flowable.just(it)
            }
        }
    }

    override fun queryAllGroups(): Flowable<List<Group>> {
        return Flowable.create({
            val groups = IMCoreManager.getImDataBase().groupDao().findAll()
            it.onNext(groups)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun reset() {
    }

    override fun onSignalReceived(type: Int, body: String) {

    }
}