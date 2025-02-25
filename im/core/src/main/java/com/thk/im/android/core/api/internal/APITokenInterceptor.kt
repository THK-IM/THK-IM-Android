package com.thk.im.android.core.api.internal


import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.vo.CodeMessage
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.exception.CodeMsgException
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer


class APITokenInterceptor(private var token: String) : Interceptor {

    companion object {
        const val tokenKey = "Authorization"
        const val versionKey = "Version"
        const val timezoneKey = "TimeZone"
        const val deviceKey = "Device"
        const val languageKey = "Accept-Language"
        const val platformKey = "Platform"
    }

    private val gson = Gson()
    private val endpoints = mutableSetOf<String>()
    private val jsonType = "application/json; charset=utf-8".toMediaTypeOrNull()

    fun updateToken(token: String) {
        this.token = token
    }

    fun addValidEndpoint(endpoint: String) {
        endpoints.add(endpoint)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = newRequest(chain.request())
        val encryptedRequest = encryptRequest(newRequest)
        val response = chain.proceed(encryptedRequest)
        val redirectResponse = if (response.isRedirect) {
            redirectResponse(response, chain)
        } else {
            response
        }
        val decryptResponse = decryptResponse(redirectResponse)
        return normalResponse(decryptResponse)
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

    private fun decryptResponse(response: Response): Response {
        if (isValidEndpoint(response.request.url.toUrl().toExternalForm())) {
            val contentType = response.headers["Content-Type"]
            contentType?.let { type ->
                if (type.contains("application/json", true)) {
                    response.body?.string()?.let { encryptText ->
                        IMCoreManager.crypto?.let {
                            val decryptText = it.decrypt(encryptText)
                            decryptText?.let { text ->
                                val newBody = text.toResponseBody(jsonType)
                                return response.newBuilder().body(newBody).build()
                            }
                        }
                    }
                }
            }
        }
        return response
    }

    private fun normalResponse(response: Response): Response {
        if (response.code in 200..299) {
            return response
        } else {
            if (response.body != null) {
                val content = response.body?.string()
                if (content != null) {
                    val contentType = response.body?.contentType()
                    if (contentType == null) {
                        LLog.e("APITokenInterceptor", "${response.request.url}")
                        throw CodeMsgException(response.code, content)
                    }
                    if (contentType.toString().contains("application/json", true)) {
                        val codeMessage = gson.fromJson(content, CodeMessage::class.java)
                        throw CodeMsgException(codeMessage.code, codeMessage.message)
                    } else {
                        throw CodeMsgException(response.code, "unknown error")
                    }
                } else {
                    throw CodeMsgException(response.code, "unknown error")
                }
            } else {
                throw CodeMsgException(response.code, "unknown error")
            }
        }
    }

    private fun newRequest(origin: Request): Request {
        val builder = if (isValidEndpoint(origin.url.toUrl().toExternalForm())) {
            val builder = origin.newBuilder()
            if (origin.header(tokenKey) == null) {
                builder.addHeader(tokenKey, "Bearer ${this.token}")
            }
            if (origin.header(versionKey) == null) {
                builder.addHeader(versionKey, AppUtils.instance().versionName)
            }
            if (origin.header(timezoneKey) == null) {
                builder.addHeader(timezoneKey, AppUtils.instance().timeZone)
            }
            if (origin.header(deviceKey) == null) {
                builder.addHeader(deviceKey, AppUtils.instance().deviceName)
            }
            if (origin.header(languageKey) == null) {
                builder.addHeader(languageKey, AppUtils.instance().language)
            }
            if (origin.header(platformKey) == null) {
                builder.addHeader(platformKey, "Android")
            }
            builder
        } else {
            val builder = origin.newBuilder()
            builder.removeHeader(tokenKey)
            builder.removeHeader(versionKey)
            builder.removeHeader(languageKey)
            builder.removeHeader(timezoneKey)
            builder.removeHeader(deviceKey)
            builder.removeHeader(platformKey)
            builder
        }
        return builder.build()
    }

    private fun encryptRequest(origin: Request): Request {
        if (isValidEndpoint(origin.url.toUrl().toExternalForm())) {
            IMCoreManager.crypto?.let {
                val buffer = Buffer()
                origin.body?.writeTo(buffer)
                if (buffer.size > 0) {
                    val originContent = buffer.readUtf8()
                    val decryptContent = it.encrypt(originContent)
                    decryptContent?.let { content ->
                        val encryptedBody = content.toRequestBody(jsonType)
                        return origin.newBuilder().method(origin.method, encryptedBody).build()
                    }
                }
            }
        }
        return origin
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