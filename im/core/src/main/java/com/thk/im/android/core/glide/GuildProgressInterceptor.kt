package com.thk.im.android.core.glide

import okhttp3.Interceptor
import okhttp3.Interceptor.*
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.ref.WeakReference


class GuildProgressInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        val url: String = request.url.toString()
        val body = response.body ?: return response
        val newResponse: Response =
            response.newBuilder().body(ProgressResponseBody(url, body)).build()
        return newResponse
    }

    companion object {

        val LISTENER_MAP: MutableMap<String, WeakReference<GlideProgressListener>> =
            HashMap<String, WeakReference<GlideProgressListener>>()

        fun addListener(url: String, listener: GlideProgressListener) {
            LISTENER_MAP[url] = WeakReference(listener)
        }

        fun removeListener(url: String) {
            LISTENER_MAP.remove(url)
        }
    }
}
