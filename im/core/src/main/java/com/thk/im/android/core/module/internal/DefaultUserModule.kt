package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.module.UserModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

open class DefaultUserModule : UserModule {

    override fun queryServerUser(id: Long): Flowable<User> {
        val now = System.currentTimeMillis()
        val user = User(
            id, "$id", "user$id", "https://picsum.photos/300/300", (id % 2).toInt(),
            null, null, now, now
        )
        IMCoreManager.getImDataBase().userDao().insertOrReplaceUsers(listOf(user))
        return Flowable.just(user)
    }


    override fun queryUser(id: Long): Flowable<User> {
        return Flowable.create<User>({
            val user = IMCoreManager.getImDataBase().userDao().queryUser(id)
            if (user != null) {
                it.onNext(user)
            } else {
                it.onNext(User(id))
            }
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            if (it.cTime == 0L) {
                return@flatMap queryServerUser(it.id).flatMap { user ->
                    Flowable.just(user)
                }
            } else {
                Flowable.just(it)
            }
        }
    }

    override fun queryUsers(ids: Set<Long>): Flowable<Map<Long, User>> {
        return Flowable.create<Map<Long, User>?>({
            val users = IMCoreManager.getImDataBase().userDao().queryUsers(ids)
            val userMap = mutableMapOf<Long, User>()
            for (u in users) {
                userMap[u.id] = u
            }
            it.onNext(userMap)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    override fun onUserInfoUpdate(user: User) {
        IMCoreManager.getImDataBase().userDao().insertOrReplaceUsers(listOf(user))
    }

    override fun onSignalReceived(type: Int, body: String) {

    }
}