package com.thk.im.preview

import android.app.Application
import android.net.Uri
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.StringUtils
import java.io.File

object VideoCache {

    private const val cacheSize = 5 * 1024 * 1024 * 1024L
    private var proxy: HttpProxyCacheServer? = null
    private var token: String = ""
    private var endpoint: String = ""
    private var app: Application? = null

    private val tokenKey = "Authorization"
    private val clientVersionKey = "Client-Version"
    private val platformKey = "Client-Platform"

    fun init(app: Application, token: String, endpoint: String) {
        this.app = app
        this.token = token
        this.endpoint = endpoint
        synchronized(this) {
            proxy = HttpProxyCacheServer.Builder(app.applicationContext)
                .maxCacheSize(cacheSize)       // 1 Gb for cache
                .cacheDirectory(getCacheDir())
                .headerInjector { url ->
                    if (url.startsWith(endpoint)) {
                        mutableMapOf(tokenKey to "Bearer $token",
                            clientVersionKey to AppUtils.instance().verName,
                            platformKey to "Android")
                    } else {
                        mutableMapOf()
                    }
                }
                .fileNameGenerator { url ->
                    getUrlFileName(url)
                }
                .maxCacheFilesCount(100)
                .build()
        }
    }

    private fun getCacheDir(): File {
        return File(app!!.cacheDir, "video-cache")
    }

    private fun getUrlFileName(url: String): String {
        if (url.startsWith(endpoint)) {
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

    fun getEndpoint(): String {
        return endpoint
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