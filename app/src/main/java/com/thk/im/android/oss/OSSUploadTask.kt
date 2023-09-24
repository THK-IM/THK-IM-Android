package com.thk.im.android.oss

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.thk.im.android.core.fileloader.LoadListener
import java.util.concurrent.atomic.AtomicBoolean

class OSSUploadTask(
    private val key: String,
    private val path: String,
    taskId: String,
    private val fileLoaderModule: OSSFileLoaderModule
) : OSSLoadTask(taskId) {

    private var running = AtomicBoolean(true)
    private var task: OSSAsyncTask<PutObjectResult>? = null

    init {
        notify(0, LoadListener.Wait)
    }

    override fun start() {
        notify(0, LoadListener.Init)
        val bucket = fileLoaderModule.getBucketName()
        val request = PutObjectRequest(bucket, key, path)
        var progress = 0
        request.progressCallback = OSSProgressCallback { _, currentSize, totalSize ->
            progress = (currentSize * 1.0f / totalSize * 100f).toInt()
            notify(progress, LoadListener.Ing)
        }
        task = fileLoaderModule.getOssClient().asyncPutObject(request, object :
            OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                notify(progress, LoadListener.Success)
            }

            override fun onFailure(
                request: PutObjectRequest?,
                clientException: ClientException?,
                serviceException: ServiceException?
            ) {
                notify(progress, LoadListener.Failed)
            }
        })
    }

    override fun cancel() {
        task?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
        }
        task = null
        running.set(false)
    }

    override fun notify(progress: Int, state: Int) {
        fileLoaderModule.notifyListeners(taskId, progress, state, key, path)
    }
}