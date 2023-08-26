package com.thk.im.android.core.api.internal

import com.thk.im.android.core.api.bean.CreateSessionBean
import com.thk.im.android.core.api.bean.ListBean
import com.thk.im.android.core.api.bean.SessionBean
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @POST("/session")
    fun createSession(
        @Body bean: CreateSessionBean
    ): Flowable<SessionBean>

    @GET("/user_session/{uid}/{sid}")
    fun querySession(
        @Path("uid") uid: Long,
        @Path("sid") sid: Long,
    ): Flowable<SessionBean>

    @GET("/user_session/latest")
    fun queryLatestSession(
        @Query("u_id") uid: Long,
        @Query("m_time") MTime: Long,
        @Query("offset") offset: Int,
        @Query("count") count: Int
    ): Flowable<ListBean<SessionBean>>


}