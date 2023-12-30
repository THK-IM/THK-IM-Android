package com.thk.im.android.core.module

import com.thk.im.android.core.db.entity.User
import io.reactivex.Flowable

interface UserModule : BaseModule {

    /**
     * 从服务器查询用户信息
     */
    fun queryServerUser(id: Long): Flowable<User>


    /**
     * 查询用户信息
     */
    fun queryUser(id: Long): Flowable<User>

    /**
     * 批量查询用户信息
     */
    fun queryUsers(ids: Set<Long>): Flowable<Map<Long, User>>

    /**
     * 【收到服务端通知】 用户信息更新
     */
    fun onUserInfoUpdate(user: User)
}
