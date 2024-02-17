package com.thk.im.android.core.api.internal

import com.thk.im.android.core.api.vo.ListVo
import com.thk.im.android.core.api.vo.MessageVo
import com.thk.im.android.core.api.vo.SessionMemberVo
import com.thk.im.android.core.api.vo.SessionVo
import com.thk.im.android.core.api.vo.UpdateUserSession
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SessionApi {

    @GET("/session/{id}/user/latest")
    fun queryLatestSessionMembers(
        @Path("id") id: Long,
        @Query("m_time") mTime: Long,
        @Query("role") role: Int?,
        @Query("count") count: Int
    ): Flowable<ListVo<SessionMemberVo>>

    @GET("/user_session/{uid}/{sid}")
    fun querySession(
        @Path("uid") uid: Long,
        @Path("sid") sid: Long,
    ): Flowable<SessionVo>

    @GET("/user_session")
    fun querySessionByEntityId(
        @Query("u_id") uid: Long,
        @Query("entity_id") entityId: Long,
        @Query("type") type: Int,
    ): Flowable<SessionVo>

    @GET("/user_session/latest")
    fun queryLatestSession(
        @Query("u_id") uid: Long,
        @Query("m_time") mTime: Long,
        @Query("offset") offset: Int,
        @Query("count") count: Int,
        @Query("types") types: Set<Int>?
    ): Flowable<ListVo<SessionVo>>

    @PUT("/user_session")
    fun updateSession(
        @Body bean: UpdateUserSession
    ): Flowable<Void>

    @DELETE("/user_session/{uid}/{sid}")
    fun deleteSession(
        @Path("uid") uid: Long,
        @Path("sid") sid: Long,
    ): Flowable<Void>


    @GET("/session/{id}/message")
    fun querySessionMessage(
        @Path("id") id: Long,
        @Query("s_id") sid: Long,
        @Query("c_time") cTime: Long,
        @Query("offset") offset: Int,
        @Query("count") count: Int,
        @Query("asc") asc: Int
    ): Flowable<ListVo<MessageVo>>


}