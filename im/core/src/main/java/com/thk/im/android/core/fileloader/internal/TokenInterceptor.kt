package com.thk.im.android.core.fileloader.internal

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor(private var token: String, private var endpoint: String) : Interceptor {

    fun updateToken(token: String, endpoint: String) {
        this.token = token
        this.endpoint = endpoint
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = newRequest(chain.request())
        val response = chain.proceed(newRequest)
        return redirectResponse(response, chain)
    }

    private fun redirectResponse(origin: Response, chain: Interceptor.Chain): Response {
        if (origin.isRedirect) {
            val location = origin.header("Location") ?: return origin
            origin.close()
            val redirectRequest = newRequest(chain.request().newBuilder().url(location).build())
            val newResponse = chain.proceed(redirectRequest)
            return redirectResponse(newResponse, chain)
        } else {
            return origin
        }
    }

    private fun newRequest(origin: Request): Request {
        val builder = if (origin.url.toUrl().toExternalForm().startsWith(endpoint)) {
            if (origin.header("Token") == null) {
                origin.newBuilder().addHeader("Token", this.token)
            } else {
                origin.newBuilder()
            }
        } else {
            if (origin.header("Token") == null) {
                origin.newBuilder()
            } else {
                origin.newBuilder().removeHeader("Token")
            }
        }
        return builder.build()
    }
}