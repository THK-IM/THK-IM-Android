package com.thk.im.android.core.api


import com.thk.im.android.core.utils.LLog
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody


class AESInterceptor : Interceptor {

    private val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = encryptRequest(chain.request())
        val response = chain.proceed(request)
        return decryptResponse(response)
    }

    private fun encryptRequest(request: Request): Request {
        LLog.d("Origin request url:: ${request.url}")
        if (request.body == null) {
            return request
        }
        val requestBody = request.body
        val buffer = okio.Buffer()
        requestBody?.writeTo(buffer)
        val requestJson = buffer.readUtf8()
        LLog.d("Origin request body :: $requestJson")
        val newRequestBody = requestJson.toRequestBody(mediaType)
        return request.newBuilder().method(request.method, newRequestBody).build()
    }

    private fun decryptResponse(response: Response): Response {
        val contentType = response.headers["Content-type"]
        contentType?.let {
            LLog.d("Origin response content type: $it")
            it.contains("application/json").let {
                response.body?.string()?.let { body ->
                    LLog.d("Origin response :: $body")
                    val newResponseBody = body.toResponseBody(mediaType)
                    return response.newBuilder().body(newResponseBody).build()
                }
            }
        }

        return response
    }
}