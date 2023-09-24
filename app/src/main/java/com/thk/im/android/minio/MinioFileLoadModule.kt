package com.thk.im.android.minio

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.fileloader.FileLoaderModule
import com.thk.im.android.core.fileloader.LoadListener
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class MinioFileLoadModule(
    app: Application,
    val endpoint: String,
    val token: String,
) : FileLoaderModule {

    private val handler = Handler(Looper.getMainLooper())

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60
    private val tokenInterceptor = TokenInterceptor(token)

    val okHttpClient = OkHttpClient.Builder().connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true)
        .followRedirects(true).addInterceptor(tokenInterceptor)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    private val downloadTaskMap =
        HashMap<String, Pair<MinioLoadTask, MutableList<WeakReference<LoadListener>>>>()
    private val uploadTaskMap =
        HashMap<String, Pair<MinioLoadTask, MutableList<WeakReference<LoadListener>>>>()

    fun notifyListeners(
        taskId: String, progress: Int, state: Int, url: String, path: String
    ) {
        synchronized(this) {
            val dListeners = downloadTaskMap[taskId]?.second
            dListeners?.let { ls ->
                ls.forEach {
                    it.get()?.let { l ->
                        if (l.notifyOnUiThread()) {
                            handler.post {
                                l.onProgress(
                                    progress, state, url, path
                                )
                            }
                        } else {
                            l.onProgress(progress, state, url, path)
                        }
                    }
                }
                if (state == LoadListener.Failed || state == LoadListener.Success) {
                    cancelDownload(taskId)
                }
            }

            val uListeners = uploadTaskMap[taskId]?.second
            uListeners?.let { ls ->
                ls.forEach {
                    it.get()?.let { l ->
                        if (l.notifyOnUiThread()) {
                            handler.post {
                                l.onProgress(
                                    progress, state, url, path
                                )
                            }
                        } else {
                            l.onProgress(progress, state, url, path)
                        }
                    }
                }
                if (state == LoadListener.Failed || state == LoadListener.Success) {
                    cancelUpload(taskId)
                }
            }
        }
    }

    override fun getTaskId(key: String, path: String, type: String): String {
        return "{$type}/{$key}/${path}"
    }

    override fun getUploadKey(sId: Long, uId: Long, fileName: String, msgClientId: Long): String {
        return "im/session_${sId}/${uId}/" + IMCoreManager.signalModule.severTime + "_${fileName}"
    }

    override fun parserUploadKey(key: String): Triple<Long, Long, String>? {
        try {
            val paths = key.split("/")
            if (paths.size != 4) {
                return null
            }
            val sessions = paths[1].split("_")
            if (sessions.size != 2) {
                return null
            }
            return Triple(sessions[1].toLong(), paths[2].toLong(), paths[3])
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun download(url: String, path: String, listener: LoadListener): String {
        synchronized(this) {
            val taskId = getTaskId(url, path, "download")
            val p = downloadTaskMap[taskId]
            if (p == null) {
                val dTask = MinioDownloadTask(url, path, taskId, this)
                dTask.start()
                val listeners = mutableListOf(WeakReference(listener))
                downloadTaskMap[taskId] = Pair(dTask, listeners)
            } else {
                p.second.add(WeakReference(listener))
            }
            return taskId
        }
    }

    override fun upload(key: String, path: String, listener: LoadListener): String {
        synchronized(this) {
            val taskId = getTaskId(key, path, "upload")
            val p = uploadTaskMap[taskId]
            if (p == null) {
                val uTask = MinioUploadTask(key, path, taskId, this)
                uTask.start()
                val listeners = mutableListOf(WeakReference(listener))
                uploadTaskMap[taskId] = Pair(uTask, listeners)
            } else {
                p.second.add(WeakReference(listener))
            }
            return taskId
        }
    }

    override fun cancelDownload(taskId: String) {
        synchronized(this) {
            val p = downloadTaskMap[taskId]
            if (p != null) {
                p.first.cancel()
                p.second.clear()
                downloadTaskMap.remove(taskId)
            }
        }
    }

    override fun cancelDownloadListener(taskId: String) {
        synchronized(this) {
            val p = downloadTaskMap[taskId]
            p?.second?.clear()
        }
    }

    override fun cancelUpload(taskId: String) {
        synchronized(this) {
            val p = uploadTaskMap[taskId]
            if (p != null) {
                p.first.cancel()
                p.second.clear()
                uploadTaskMap.remove(taskId)
            }
        }
    }

    override fun cancelUploadListener(taskId: String) {
        synchronized(this) {
            val p = uploadTaskMap[taskId]
            p?.second?.clear()
        }
    }
}