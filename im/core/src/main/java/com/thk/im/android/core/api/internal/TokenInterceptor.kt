package com.thk.im.android.core.api.internal


import okhttp3.Interceptor
import okhttp3.Response


class TokenInterceptor(private var token: String) : Interceptor {

    private val headTokenKey = "Authorization"

    fun updateToken(token: String) {
        this.token = token
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader(headTokenKey, "Bearer ${this.token}")
        return chain.proceed(builder.build())
    }

}