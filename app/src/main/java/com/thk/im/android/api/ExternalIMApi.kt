package com.thk.im.android.api

import com.thk.im.android.api.contact.vo.CreateSessionReq
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.api.internal.DefaultIMApi
import com.thk.im.android.core.db.entity.Session
import io.reactivex.Flowable

class ExternalIMApi(serverUrl: String, token: String) : DefaultIMApi(serverUrl, token) {
    override fun createSession(
        uId: Long,
        sessionType: Int,
        name: String,
        remark: String,
        entityId: Long,
        members: Set<Long>?
    ): Flowable<Session> {
        if (sessionType == SessionType.Single.value) {
            val req = CreateSessionReq(uId, entityId)
            return UserRepository.contactApi.createSession(req)
                .flatMap {
                    return@flatMap Flowable.just(it.toSession())
                }
        }
        return super.createSession(uId, sessionType, name, remark, entityId, members)
    }
}