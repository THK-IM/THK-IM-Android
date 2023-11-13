package com.thk.im.android.core.fileloader.internal

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.fileloader.FileLoadModule
import com.thk.im.android.core.fileloader.FileLoadState
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.db.entity.Message
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DefaultFileLoadModule(
    app: Application,
    val endpoint: String,
    val token: String,
) : FileLoadModule {


    val cacheDir = File(app.cacheDir, "message_cache")
    private val cacheExpire = 10 * 24 * 3600 * 1000L
    private val handler = Handler(Looper.getMainLooper())

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 4
    private val keepAliveDuration: Long = 60
    private val tokenInterceptor = TokenInterceptor(token, endpoint)

    val okHttpClient = OkHttpClient.Builder().connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor(tokenInterceptor)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    private val downloadTaskMap =
        ConcurrentHashMap<String, Pair<LoadTask, MutableList<LoadListener>>>()
    private val uploadTaskMap =
        ConcurrentHashMap<String, Pair<LoadTask, MutableList<LoadListener>>>()

    init {
        okHttpClient.dispatcher.maxRequests = 32
        okHttpClient.dispatcher.maxRequestsPerHost = 16
        if (cacheDir.exists()) {
            if (cacheDir.isFile) {
                cacheDir.delete()
                cacheDir.mkdirs()
            }
        } else {
            cacheDir.mkdirs()
        }
        val files = cacheDir.listFiles()
        files?.let {
            for (f in files) {
                if (kotlin.math.abs(f.lastModified() - System.currentTimeMillis()) > cacheExpire) {
                    f.delete()
                }
            }
        }
    }

    fun notifyListeners(
        progress: Int, state: Int, url: String, path: String, exception: Exception?
    ) {
        val dListeners = downloadTaskMap[url]?.second
        dListeners?.let { ls ->
            ls.forEach {
                notify(it, progress, state, url, path, exception)
            }
        }

        val uListeners = uploadTaskMap[path]?.second
        uListeners?.let { ls ->
            ls.forEach {
                notify(it, progress, state, url, path, exception)
            }
        }

        if (state == FileLoadState.Failed.value || state == FileLoadState.Success.value) {
            cancelUpload(path)
            cancelDownload(url)
        }
    }

    private fun notify(listener: LoadListener, progress: Int, state: Int, url: String, path: String, exception: Exception?) {
        if (listener.notifyOnUiThread()) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                listener.onProgress(
                    progress, state, url, path, exception
                )
            } else {
                handler.post {
                    listener.onProgress(
                        progress, state, url, path, exception
                    )
                }
            }
        } else {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                Thread {
                    kotlin.run {
                        listener.onProgress(progress, state, url, path, exception)
                    }
                }.start()
            } else {
                listener.onProgress(progress, state, url, path, exception)
            }
        }
    }

    private fun buildUploadParam(path: String, message: Message): String {
        val (_, fileName) = IMCoreManager.storageModule.getPathsFromFullPath(path)
        return "s_id=${message.sid}&u_id=${message.fUid}&f_name=${fileName}&client_id=${message.id}"
    }

    private fun buildDownloadParam(key: String, message: Message): String {
        if (key.startsWith("http")) {
            return ""
        }
        return "s_id=${message.sid}&id=${key}"
    }

    override fun download(key: String, message: Message, listener: LoadListener) {
        val p = downloadTaskMap[key]
        if (p == null) {
            val downloadParam = this.buildDownloadParam(key, message)
            val dTask = DownloadTask(key, downloadParam, this)
            val listeners = mutableListOf(listener)
            downloadTaskMap[key] = Pair(dTask, listeners)
            dTask.start()
        } else {
            if (!p.second.contains(listener)) {
                p.second.add(listener)
            }
        }
    }

    override fun upload(path: String, message: Message, listener: LoadListener) {
        val p = uploadTaskMap[path]
        if (p == null) {
            val uploadParam = this.buildUploadParam(path, message)
            val uTask = UploadTask(path, uploadParam, this)
            val listeners = mutableListOf(listener)
            uploadTaskMap[path] = Pair(uTask, listeners)
            uTask.start()
        } else {
            if (!p.second.contains(listener)) {
                p.second.add(listener)
            }
        }
    }

    override fun cancelDownload(url: String) {
        val p = downloadTaskMap[url]
        if (p != null) {
            p.second.clear()
            p.first.cancel()
            downloadTaskMap.remove(url)
        }
    }

    override fun cancelDownloadListener(url: String) {
        val p = downloadTaskMap[url]
        p?.second?.clear()
    }

    override fun cancelUpload(path: String) {
        val p = uploadTaskMap[path]
        if (p != null) {
            p.first.cancel()
            p.second.clear()
            uploadTaskMap.remove(path)
        }
    }

    override fun cancelUploadListener(path: String) {
        val p = uploadTaskMap[path]
        p?.second?.clear()
    }
}