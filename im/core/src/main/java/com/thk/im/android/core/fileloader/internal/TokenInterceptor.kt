package com.thk.im.android.core.fileloader.internal

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private var token: String) : Interceptor {

    fun updateToken(token: String) {
        this.token = token
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder().addHeader("Token", this.token).build()
        return chain.proceed(newRequest)
    }
}