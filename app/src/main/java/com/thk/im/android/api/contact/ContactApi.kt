package com.thk.im.android.api.contact

import com.thk.im.android.api.contact.vo.ContactSessionCreateVo
import com.thk.im.android.api.contact.vo.ContactVo
import com.thk.im.android.core.api.vo.ListVo
import com.thk.im.android.core.api.vo.PageListVo
import com.thk.im.android.core.api.vo.SessionVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContactApi {

    /**
     * 创建会话
     */
    @POST("/contact/session")
    fun createContactSession(
        @Body body: ContactSessionCreateVo
    ): Flowable<SessionVo>


    @GET("/contact")
    fun queryContactList(
        @Query("u_id") uId: Long,
        @Query("relation_type") relationType: Int,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
    ): Flowable<PageListVo<ContactVo>>

    @GET("/contact/latest")
    fun queryLatestContactList(
        @Query("u_id") uId: Long,
        @Query("m_time") mTime: Long,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
    ): Flowable<ListVo<ContactVo>>

}