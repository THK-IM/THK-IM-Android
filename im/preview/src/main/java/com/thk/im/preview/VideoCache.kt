package com.thk.im.preview

import android.app.Application
import com.danikula.videocache.CacheListener
import com.danikula.videocache.HttpProxyCacheServer
import com.thk.im.android.core.IMCoreManager
import java.io.File

object VideoCache {

    private const val cacheSize = 5 * 1024 * 1024 * 1024L
    private var proxy: HttpProxyCacheServer? = null
    private var token: String = ""
    private var app: Application? = null

    fun init(app: Application, token: String) {
        this.app = app
        this.token = token
        synchronized(this) {
            proxy = HttpProxyCacheServer.Builder(app.applicationContext)
                .maxCacheSize(cacheSize)       // 1 Gb for cache
                .cacheDirectory(getCacheDir())
                .headerInjector { mutableMapOf("Token" to token) }
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
        val pair = IMCoreManager.storageModule.getPathsFromFullPath(url)
        return pair.second
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

    fun getCachePath(url: String): String? {
        val file = File(getCacheDir(), getUrlFileName(url))
        return if (file.exists()) {
            file.absolutePath
        } else {
            null
        }
    }

}