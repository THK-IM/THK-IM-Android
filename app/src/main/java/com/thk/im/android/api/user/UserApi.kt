package com.thk.im.android.api.user

import com.thk.im.android.api.user.vo.BasicUserInfo
import com.thk.im.android.api.user.vo.LoginResp
import com.thk.im.android.api.user.vo.TokenLoginReq
import com.thk.im.android.api.user.vo.User
import com.thk.im.android.api.user.vo.UserRegisterReq
import com.thk.im.android.api.user.vo.UserRegisterResp
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    // 用户查询自己的信息
    @GET("/user/query/:id")
    fun queryUser(
        @Path("id") id: Long
    ): Flowable<User>

    // 用户查询他人的信息
    @GET("/user/query")
    fun searchUser(
        @Query("id") displayId: String
    ): Flowable<BasicUserInfo>

}