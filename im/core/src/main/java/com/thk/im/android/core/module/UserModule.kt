package com.thk.im.android.core.module;

import com.thk.im.android.core.api.bean.UserBean
import com.thk.im.android.db.entity.User
import io.reactivex.Flowable

interface UserModule : CommonModule{

    fun getServerUserInfo(id: Long): Flowable<UserBean>


    /**
     * 【用户主动发起】 获取用户信息
     */
    fun getUserInfo(id: Long): Flowable<User>

    /**
     * 【用户主动发起】 获取用户信息
     */
    fun updateUserInfo(userBean: UserBean): Flowable<Void>

    /**
     * 【收到服务端通知】 用户信息更新
     */
    fun onUserInfoUpdate(userBean: UserBean)
}
