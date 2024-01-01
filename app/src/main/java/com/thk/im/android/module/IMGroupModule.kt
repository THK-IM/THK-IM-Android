package com.thk.im.android.module

import com.thk.im.android.api.DataRepository
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.module.internal.DefaultGroupModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class IMGroupModule : DefaultGroupModule() {

    override fun findOne(id: Long): Flowable<Group> {
        return Flowable.create<Group?>({
            val group = IMCoreManager.getImDataBase().groupDao().findOne(id)
            if (group != null) {
                it.onNext(group)
            } else {
                it.onNext(Group(id))
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.cTime == 0L) {
                return@flatMap DataRepository.groupApi.queryGroup(it.id)
                    .flatMap { vo ->
                        val group = vo.toGroup()
                        IMCoreManager.db.groupDao().insertOrUpdateGroups(listOf(group))
                        Flowable.just(group)
                    }

            } else {
                Flowable.just(it)
            }
        }
    }

    override fun queryAllGroups(): Flowable<List<Group>> {
        return Flowable.create({
            val groups = IMCoreManager.getImDataBase().groupDao().queryAllGroups()
            it.onNext(groups)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }
}