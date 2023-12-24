package com.thk.im.android.api.user

import com.thk.im.android.api.user.vo.LoginResp
import com.thk.im.android.api.user.vo.TokenLoginReq
import com.thk.im.android.api.user.vo.UserRegisterReq
import com.thk.im.android.api.user.vo.UserRegisterResp
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST
interface UserApi {

    /**
     * 注册
     */
    @POST("/user/register")
    fun register(
        @Body body: UserRegisterReq
    ): Flowable<UserRegisterResp>

    /**
     * 通过token登录
     */
    @POST("/user/login/token")
    fun loginByToken(
        @Body body: TokenLoginReq
    ): Flowable<LoginResp>
}