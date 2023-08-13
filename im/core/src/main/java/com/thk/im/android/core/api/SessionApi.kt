package com.thk.im.android.core.api

import com.thk.im.android.core.bean.CreateSessionBean
import com.thk.im.android.core.bean.SessionBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @POST("/im/v3/session")
    fun createSession(
        @Body bean: CreateSessionBean
    ): Flowable<SessionBean>

    @GET("/im/v3/user_session/{uid}/{sid}")
    fun querySession(
        @Path("uid") uid: Long,
        @Path("sid") sid: Long,
    ): Flowable<SessionBean>

    @GET("/im/v3/session/latest")
    fun queryLatestSession(
        @Query("u_id") uid: Long,
        @Query("m_time") MTime: Long,
        @Query("offset") offset: Int,
        @Query("size") size: Int
    ): Flowable<List<SessionBean>>


}