package com.thk.im.android.api.contact

import com.thk.im.android.api.contact.vo.CreateSessionReq
import com.thk.im.android.core.api.vo.SessionVo
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface ContactApi {
    /**
     * 创建会话
     */
    @POST("/contact/session")
    fun createSession(
        @Body body: CreateSessionReq
    ): Flowable<SessionVo>
}