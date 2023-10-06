package com.thk.im.android.media.preview

import android.content.Context
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator
import com.thk.im.android.core.IMCoreManager
import java.io.File

object VideoCache {

    private const val cacheSize = 5 * 1024 * 1024 * 1024L

    private var proxy: HttpProxyCacheServer? = null

    private var token: String = ""

    fun init(context: Context, token: String) {
        synchronized(this) {
            this.token = token
            if (proxy == null) {
                proxy = HttpProxyCacheServer.Builder(context.applicationContext)
                    .maxCacheSize(cacheSize)       // 1 Gb for cache
                    .cacheDirectory(File(context.cacheDir, "video-cache"))
                    .headerInjector { mutableMapOf("Token" to token) }
                    .fileNameGenerator { url ->
                        val pair = IMCoreManager.storageModule.getPathsFromFullPath(url)
                        pair.second
                    }
                    .maxCacheFilesCount(100)
                    .build()
            }
        }
    }

    fun getProxy(): HttpProxyCacheServer {
        return proxy!!
    }
}