package com.thk.im.android.api.user

import com.thk.im.android.api.user.vo.LoginVo
import com.thk.im.android.api.user.vo.RegisterReq
import com.thk.im.android.api.user.vo.RegisterVo
import com.thk.im.android.api.user.vo.TokenLoginReq
import com.thk.im.android.api.user.vo.UserBasicInfoVo
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
        @Body body: RegisterReq
    ): Flowable<RegisterVo>

    /**
     * 通过token登录
     */
    @POST("/user/login/token")
    fun loginByToken(
        @Body body: TokenLoginReq
    ): Flowable<LoginVo>

    // 用户查询自己的信息
    @GET("/user/query/{id}")
    fun queryUser(
        @Path("id") id: Long
    ): Flowable<UserBasicInfoVo>

    // 用户查询他人的信息
    @GET("/user/query")
    fun searchUserByDisplayId(
        @Query("display_id") displayId: String
    ): Flowable<UserBasicInfoVo>

}