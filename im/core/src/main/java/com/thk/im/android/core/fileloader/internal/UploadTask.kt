package com.thk.im.android.core.fileloader.internal

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.thk.im.android.core.fileloader.LoadListener
import java.util.concurrent.atomic.AtomicBoolean

class UploadTask(
    private val key: String,
    private val path: String,
    taskId: String,
    private val fileLoaderModule: DefaultFileLoaderModule
) : FileTask(taskId) {

    private var running = AtomicBoolean(true)

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
        fileLoaderModule.getOssClient().asyncPutObject(request, object :
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
        running.set(false)
    }

    override fun notify(progress: Int, state: Int) {
        fileLoaderModule.notifyListeners(taskId, progress, state, key, path)
    }
}