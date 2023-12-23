package com.thk.im.android.ui.welcome.api

import com.thk.im.android.ui.welcome.api.vo.UserRegisterReq
import com.thk.im.android.ui.welcome.api.vo.UserRegisterResp
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface WelcomeApi {

    /**
     * 发送消息
     */
    @POST("/register")
    fun register(
        @Body body: UserRegisterReq
    ): Flowable<UserRegisterResp>
}