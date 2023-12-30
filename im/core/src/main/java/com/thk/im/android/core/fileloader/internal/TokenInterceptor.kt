package com.thk.im.android.core.fileloader.internal

import com.thk.im.android.core.base.utils.AppUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor(private var token: String, private var endpoint: String) : Interceptor {

    private val tokenKey = "Authorization"
    private val clientVersionKey = "Client-Version"
    private val platformKey = "Client-Platform"

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
            if (origin.header(tokenKey) == null) {
                val builder =  origin.newBuilder()
                builder.addHeader(tokenKey, "Bearer ${this.token}")
                builder.addHeader(clientVersionKey, AppUtils.instance().verName)
                builder.addHeader(platformKey, "Android")
            } else {
                origin.newBuilder()
            }
        } else {
            if (origin.header(tokenKey) == null) {
                origin.newBuilder()
            } else {
                origin.newBuilder().removeHeader(tokenKey)
            }
        }
        return builder.build()
    }
}