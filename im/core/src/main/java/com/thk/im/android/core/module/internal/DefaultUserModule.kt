package com.thk.im.android.core.module.internal

import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.bean.UserBean
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.db.entity.User
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

open class DefaultUserModule : UserModule {

    override fun getServerUserInfo(id: Long): Flowable<UserBean> {
        val now = System.currentTimeMillis()
        val userBean = UserBean(
            id, "user$id", "https://picsum.photos/id/${id % 1000}/200/200",
            (id % 2).toInt(), now, now
        )
        return Flowable.just(userBean)
    }

    override fun getUserInfo(id: Long): Flowable<User> {
        return Flowable.create<User>({
            val user = IMCoreManager.getImDataBase().userDao().queryUser(id)
            if (user != null) {
                it.onNext(user)
            } else {
                it.onNext(User(id))
            }
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
        }.compose(RxTransform.flowableToMain())
    }

    override fun updateUserInfo(userBean: UserBean): Flowable<Void> {
        TODO("Not yet implemented")
    }

    override fun onUserInfoUpdate(userBean: UserBean) {

    }

    override fun onSignalReceived(subType: Int, body: String) {

    }
}