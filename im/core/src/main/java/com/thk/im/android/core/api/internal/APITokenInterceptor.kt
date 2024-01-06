package com.thk.im.android.core.api.internal


import com.google.gson.Gson
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.exception.HttpCodeMessageException
import com.thk.im.android.core.exception.CodeMessage
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class APITokenInterceptor(private var token: String) : Interceptor {

    companion object {
        const val tokenKey = "Authorization"
        const val clientVersionKey = "Client-Version"
        const val platformKey = "Client-Platform"
    }

    private val gson = Gson()
    private val endpoints = mutableSetOf<String>()

    fun updateToken(token: String) {
        this.token = token
    }

    fun addValidEndpoint(endpoint: String) {
        endpoints.add(endpoint)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = newRequest(chain.request())
        val response = chain.proceed(newRequest)
        return if (response.isRedirect) {
            val newResponse = redirectResponse(response, chain)
            normalResponse(newResponse)
        } else {
            normalResponse(response)
        }

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

    private fun normalResponse(response: Response): Response {
        if (response.code in 200..399) {
            return response
        } else {
            if (response.body != null) {
                val content = response.body?.string()
                if (content != null) {
                    val contentType = response.body?.contentType()
                    if (contentType == null) {
                        val codeMessage = CodeMessage(response.code, content)
                        throw HttpCodeMessageException(codeMessage)
                    }
                    if (contentType.toString().contains("application/json", true)) {
                        val codeMessage = gson.fromJson(content, CodeMessage::class.java)
                        throw HttpCodeMessageException(codeMessage)
                    } else {
                        val codeMessage = CodeMessage(response.code, content)
                        throw HttpCodeMessageException(codeMessage)
                    }
                } else {
                    val codeMessage = CodeMessage(response.code, "unknown error")
                    throw HttpCodeMessageException(codeMessage)
                }
            } else {
                val codeMessage = CodeMessage(response.code, "unknown error")
                throw HttpCodeMessageException(codeMessage)
            }
        }
    }

    private fun newRequest(origin: Request): Request {
        val builder = if (isValidEndpoint(origin.url.toUrl().toExternalForm())) {
            val builder = origin.newBuilder()
            if (origin.header(tokenKey) == null) {
                builder.addHeader(tokenKey, "Bearer ${this.token}")
            }
            if (origin.header(clientVersionKey) == null) {
                builder.addHeader(clientVersionKey, AppUtils.instance().verName)
            }
            if (origin.header(platformKey) == null) {
                builder.addHeader(platformKey, "Android")
            }
            builder
        } else {
            val builder = origin.newBuilder()
            builder.removeHeader(tokenKey)
            builder.removeHeader(clientVersionKey)
            builder.removeHeader(platformKey)
            builder
        }
        return builder.build()
    }

    private fun isValidEndpoint(url: String): Boolean {
        for (edp in endpoints) {
            if (url.startsWith(edp, true)) {
                return true
            }
        }
        return false
    }

}