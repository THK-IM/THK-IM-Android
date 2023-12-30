package com.thk.im.android.module

import com.thk.im.android.api.DataRepository
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.module.internal.DefaultUserModule
import io.reactivex.Flowable

class IMUserModule: DefaultUserModule() {

    override fun queryServerUser(id: Long): Flowable<User> {
        return DataRepository.userApi.queryUser(id).flatMap {
            val user = it.toUser()
            IMCoreManager.getImDataBase().userDao().insertOrReplaceUsers(listOf(user))
            return@flatMap Flowable.just(user)
        }
    }
}