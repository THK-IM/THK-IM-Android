package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.module.GroupModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

open class DefaultGroupModule : GroupModule {

    override fun findOne(id: Long): Flowable<Group> {
        return Flowable.create({
            val group = IMCoreManager.getImDataBase().groupDao().findById(id)
            if (group != null) {
                it.onNext(group)
            } else {
                it.onError(FileNotFoundException())
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun queryAllGroups(): Flowable<List<Group>> {
        return Flowable.create({
            val groups = IMCoreManager.getImDataBase().groupDao().findAll()
            it.onNext(groups)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun onSignalReceived(type: Int, body: String) {

    }
}