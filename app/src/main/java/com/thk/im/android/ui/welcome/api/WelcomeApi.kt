package com.thk.im.android.ui.welcome.api

import com.thk.im.android.ui.welcome.api.vo.LoginResp
import com.thk.im.android.ui.welcome.api.vo.TokenLoginReq
import com.thk.im.android.ui.welcome.api.vo.UserRegisterReq
import com.thk.im.android.ui.welcome.api.vo.UserRegisterResp
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface WelcomeApi {

    /**
     * 注册
     */
    @POST("/user/register")
    fun register(
        @Body body: UserRegisterReq
    ): Flowable<UserRegisterResp>

    /**
     * 注册
     */
    @POST("/user/login/token")
    fun loginByToken(
        @Body body: TokenLoginReq
    ): Flowable<LoginResp>
}