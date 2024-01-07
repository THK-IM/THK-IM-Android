package com.thk.im.android.module

import com.thk.im.android.api.DataRepository
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.module.internal.DefaultGroupModule
import io.reactivex.Flowable

class IMGroupModule : DefaultGroupModule() {

    override fun queryServerGroup(id: Long): Flowable<Group?> {
        return DataRepository.groupApi.queryGroup(id)
            .flatMap { vo ->
                val group = vo.toGroup()
                Flowable.just(group)
            }
    }
}