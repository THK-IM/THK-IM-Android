package com.thk.im.preview

import android.app.Application
import android.net.Uri
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.thk.im.android.core.api.internal.APITokenInterceptor
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.StringUtils
import java.io.File

object VideoCache {

    private const val cacheSize = 5 * 1024 * 1024 * 1024L
    private var proxy: HttpProxyCacheServer? = null
    private lateinit var app: Application
    private val tokenMap = mutableMapOf<String, String>()

    fun init(app: Application) {
        this.app = app
        synchronized(this) {
            proxy = HttpProxyCacheServer.Builder(app.applicationContext)
                .maxCacheSize(cacheSize)       // 1 Gb for cache
                .cacheDirectory(getCacheDir())
                .headerInjector { url ->
                    val header = mutableMapOf<String, String>()
                    header[APITokenInterceptor.versionKey] = AppUtils.instance().versionName
                    header[APITokenInterceptor.timezoneKey] = AppUtils.instance().timeZone
                    header[APITokenInterceptor.deviceKey] = AppUtils.instance().deviceName
                    header[APITokenInterceptor.languageKey] = AppUtils.instance().language
                    header[APITokenInterceptor.platformKey] = "Android"
                    tokenMap[url]?.let {
                        header[APITokenInterceptor.tokenKey] = "Bearer $tokenMap[url]"
                    }
                    header
                }
                .fileNameGenerator { url ->
                    getUrlFileName(url)
                }
                .maxCacheFilesCount(100)
                .build()
        }
    }

    private fun getCacheDir(): File {
        return File(app.cacheDir, "video-cache")
    }

    private fun getUrlFileName(url: String): String {
        if (tokenMap[url] != null) {
            val uri = Uri.parse(url)
            val id = uri.getQueryParameter("id")
            if (id != null) {
                return id
            }
        }
        return StringUtils.shaEncrypt(url)

    }

    fun getProxy(): HttpProxyCacheServer {
        return proxy!!
    }

    fun registerCacheListener(listener: CacheListener, url: String) {
        proxy?.registerCacheListener(listener, url)
    }

    fun unregister(listener: CacheListener) {
        proxy?.unregisterCacheListener(listener)
    }

    fun addEndpointToken(endpoint: String, token: String) {
        tokenMap[endpoint] = token
    }

    fun getCachePath(url: String): String? {
        val file = File(getCacheDir(), getUrlFileName(url))
        return if (file.exists()) {
            file.absolutePath
        } else {
            null
        }
    }

}