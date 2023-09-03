package com.thk.im.android.oss

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.fileloader.FileLoaderModule
import com.thk.im.android.core.fileloader.LoadListener
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class OSSFileLoaderModule(
    app: Application,
    private val bucket: String,
    private val endpoint: String,
    val token: String,
    credentialProvider: OSSFederationCredentialProvider
) : FileLoaderModule {

    private val handler = Handler(Looper.getMainLooper())

    private val scheme = "https://"

    private val ossClient: OSSClient

    private val defaultTimeout: Long = 30
    private val maxIdleConnection = 8
    private val keepAliveDuration: Long = 60

    val downloadClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
        .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
        .readTimeout(defaultTimeout, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .connectionPool(ConnectionPool(maxIdleConnection, keepAliveDuration, TimeUnit.SECONDS))
        .build()

    init {
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000
        conf.socketTimeout = 15 * 1000
        conf.maxConcurrentRequest = 5
        conf.maxErrorRetry = 2
        ossClient = OSSClient(app, endpoint, credentialProvider, conf)
    }

    fun getOssClient(): OSSClient {
        return ossClient
    }

    fun getBucketName(): String {
        return bucket
    }

    private val downloadTaskMap =
        HashMap<String, Pair<OSSLoadTask, MutableList<WeakReference<LoadListener>>>>()
    private val uploadTaskMap =
        HashMap<String, Pair<OSSLoadTask, MutableList<WeakReference<LoadListener>>>>()


    fun notifyListeners(
        taskId: String,
        progress: Int,
        state: Int,
        url: String,
        path: String
    ) {
        synchronized(this) {
            val dListeners = downloadTaskMap[taskId]?.second
            dListeners?.let { ls ->
                ls.forEach {
                    it.get()?.let { l ->
                        if (l.notifyOnUiThread()) {
                            handler.post {
                                l.onProgress(
                                    progress,
                                    state,
                                    url,
                                    path
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
                                    progress,
                                    state,
                                    "$scheme$bucket.$endpoint/${url}",
                                    path
                                )
                            }
                        } else {
                            l.onProgress(progress, state, "$scheme$bucket.$endpoint/${url}", path)
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

    override fun download(url: String, path: String, listener: LoadListener): String {
        synchronized(this) {
            val taskId = getTaskId(url, path, "download")
            val p = downloadTaskMap[taskId]
            if (p == null) {
                val dTask = OSSDownloadTask(url, path, taskId, this)
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
                val uTask = OSSUploadTask(key, path, taskId, this)
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