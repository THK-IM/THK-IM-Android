package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.bean.UserBean
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.core.module.UserModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

open class DefaultUserModule : UserModule {

    override fun getServerUserInfo(id: Long): Flowable<UserBean> {
        val now = System.currentTimeMillis()
        val userBean = UserBean(
            id, "user$id", "https://picsum.photos/300/300",
            (id % 2).toInt(), now, now
        )
        return Flowable.just(userBean)
    }

    override fun getServerUsersInfo(ids: Set<Long>): Flowable<List<UserBean>> {
        val now = System.currentTimeMillis()
        val userBeans = mutableListOf<UserBean>()
        for (id in ids) {
            val userBean = UserBean(
                id, "user$id", "https://picsum.photos/300/300",
                (id % 2).toInt(), now, now
            )
            userBeans.add(userBean)
        }
        return Flowable.just(userBeans)
    }

    override fun getUserInfo(id: Long): Flowable<User> {
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
                return@flatMap getServerUserInfo(it.id).flatMap { bean ->
                    val user = bean.toUser()
                    IMCoreManager.getImDataBase().userDao().insertUsers(user)
                    Flowable.just(user)
                }
            } else {
                Flowable.just(it)
            }
        }
    }

    override fun getUserInfo(ids: Set<Long>): Flowable<Map<Long, User>> {
        return Flowable.create<Map<Long, User>?>({
            val users = IMCoreManager.getImDataBase().userDao().queryUsers(ids)
            val userMap = mutableMapOf<Long, User>()
            for (u in users) {
                userMap[u.id] = u
            }
            it.onNext(userMap)
            it.onComplete()
        }, BackpressureStrategy.LATEST).flatMap {
            val userNotInDb = mutableSetOf<Long>()
            for (id in ids) {
                if (it[id] == null) {
                    userNotInDb.add(id)
                }
            }
            if (userNotInDb.isEmpty()) {
                return@flatMap Flowable.just(it)
            } else {
                val result = mutableMapOf<Long, User>()
                result.putAll(it)
                return@flatMap getServerUsersInfo(userNotInDb).flatMap { beans ->
                    for (bean in beans) {
                        val user = bean.toUser()
                        IMCoreManager.getImDataBase().userDao().insertUsers(user)
                        result[user.id] = user
                    }
                    Flowable.just(result)
                }
            }
        }
    }

    override fun updateUserInfo(userBean: UserBean): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun onUserInfoUpdate(userBean: UserBean) {

    }

    override fun onSignalReceived(subType: Int, body: String) {

    }
}