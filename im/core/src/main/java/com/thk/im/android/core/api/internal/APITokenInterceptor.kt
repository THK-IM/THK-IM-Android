package com.thk.im.android.core.api.internal


import com.google.gson.Gson
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.exception.HttpCodeMessageException
import com.thk.im.android.core.exception.CodeMessage
import okhttp3.Interceptor
import okhttp3.Response


class APITokenInterceptor(private var token: String) : Interceptor {

    private val tokenKey = "Authorization"
    private val clientVersionKey = "Client-Version"
    private val platformKey = "Client-Platform"

    private val gson = Gson()

    fun updateToken(token: String) {
        this.token = token
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader(tokenKey, "Bearer ${this.token}")
        builder.addHeader(clientVersionKey, AppUtils.instance().verName)
        builder.addHeader(platformKey, "Android")
        val response = chain.proceed(builder.build())
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
                    val codeMessage = CodeMessage(response.code, "empty")
                    throw HttpCodeMessageException(codeMessage)
                }
            } else {
                val codeMessage = CodeMessage(response.code, "empty")
                throw HttpCodeMessageException(codeMessage)
            }
        }
    }

}