package com.thk.im.android.core.api.internal


import com.thk.im.android.core.base.utils.AppUtils
import okhttp3.Interceptor
import okhttp3.Response


class TokenInterceptor(private var token: String) : Interceptor {

    private val tokenKey = "Authorization"
    private val clientVersionKey = "Client-Version"
    private val platformKey = "Client-Platform"

    fun updateToken(token: String) {
        this.token = token
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader(tokenKey, "Bearer ${this.token}")
        builder.addHeader(clientVersionKey, AppUtils.instance().verName)
        builder.addHeader(platformKey, "Android")
        return chain.proceed(builder.build())
    }

}