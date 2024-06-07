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
        IMCoreManager.getImDataBase().userDao().insertOrReplace(listOf(user))
        return Flowable.just(user)
    }

    open fun queryServerUsers(ids: Set<Long>): Flowable<Map<Long, User>> {
        val userMap = mutableMapOf<Long, User>()
        for (id in ids) {
            userMap[id] = User(id)
        }
        return Flowable.just(userMap)
    }


    override fun queryUser(id: Long): Flowable<User> {
        return Flowable.create<User>({
            val user = IMCoreManager.getImDataBase().userDao().findById(id)
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
            val users = IMCoreManager.getImDataBase().userDao().findByIds(ids)
            val dbUserMap = mutableMapOf<Long, User>()
            for (u in users) {
                dbUserMap[u.id] = u
            }
            it.onNext(dbUserMap)
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap { dbUserMap ->
            val notFoundIds = mutableSetOf<Long>()
            for (id in ids) {
                if (dbUserMap[id] == null) {
                    notFoundIds.add(id)
                }
            }
            if (notFoundIds.isEmpty()) {
                return@flatMap Flowable.just(dbUserMap)
            } else {
                return@flatMap queryServerUsers(notFoundIds).flatMap { serverUserMap ->
                    val fullUserMap = mutableMapOf<Long, User>()
                    for ((k, v) in serverUserMap) {
                        fullUserMap[k] = v
                    }
                    for ((k, v) in dbUserMap) {
                        fullUserMap[k] = v
                    }
                    Flowable.just(fullUserMap)
                }
            }
        }
    }

    override fun onUserInfoUpdate(user: User) {
        IMCoreManager.getImDataBase().userDao().insertOrReplace(listOf(user))
    }

    override fun reset() {
    }

    override fun onSignalReceived(type: Int, body: String) {

    }
}