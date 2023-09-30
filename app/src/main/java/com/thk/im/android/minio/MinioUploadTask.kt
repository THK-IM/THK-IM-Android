package com.thk.im.android.minio

import com.google.gson.Gson
import com.thk.im.android.base.LLog
import com.thk.im.android.core.fileloader.LoadListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


class MinioUploadTask(
    private val key: String,
    private val path: String,
    taskId: String,
    private val fileLoaderModule: MinioFileLoadModule
) : MinioLoadTask(taskId) {

    private var running = AtomicBoolean(true)
    private var call: Call? = null
    private var keyUrl: String? = null

    init {
        notify(0, LoadListener.Wait)
    }

    override fun start() {
        LLog.i("MinioUploadTask start")
        val params = fileLoaderModule.parserUploadKey(key)
        if (params == null) {
            notify(0, LoadListener.Failed)
            return
        }
        // first 调用api获取上传参数
        val url =
            "${fileLoaderModule.endpoint}/object/upload_params?" + "s_id=${params.first}&u_id=${params.second}&f_name=${params.third}"
        val request = Request.Builder().url(url).build()
        fileLoaderModule.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, LoadListener.Failed)
                LLog.e("MinioUploadTask failed $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    if (response.body == null) {
                        LLog.e("MinioUploadTask failed get params error")
                        notify(0, LoadListener.Failed)
                    } else {
                        val json = response.body?.string()
                        val uploadParams = Gson().fromJson(json, UploadParams::class.java)
                        startUpload(uploadParams)
                    }
                } else {
                    LLog.e("MinioUploadTask failed get params ${response.code}")
                    notify(0, LoadListener.Failed)
                }
            }
        })
    }

    private fun startUpload(uploadParams: UploadParams) {
        if (!running.get()) {
            notify(0, LoadListener.Failed)
            return
        }
        notify(0, LoadListener.Init)
        val requestBodyBuilder = MultipartBody.Builder()
        requestBodyBuilder.setType(MultipartBody.FORM)
        for ((k, v) in uploadParams.params) {
            requestBodyBuilder.addFormDataPart(k, v)
        }
        val file = File(path)
        val fileBody: RequestBody =
            FileProgressRequestBody(file, object : FileProgressRequestBody.ProgressListener {
                override fun transferred(size: Long, progress: Int) {
                    notify(progress, LoadListener.Init)
                }
            })
        requestBodyBuilder.addFormDataPart("file", file.name, fileBody)
        val request = Request.Builder().method(uploadParams.method, requestBodyBuilder.build())
            .url(uploadParams.url).build()
        call = fileLoaderModule.okHttpClient.newCall(request)
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, LoadListener.Failed)
                LLog.e("MinioUploadTask failed $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    LLog.i("MinioUploadTask success ${response.code}")
                    keyUrl = "${fileLoaderModule.endpoint}/object/${uploadParams.id}"
                    notify(100, LoadListener.Success)
                } else {
                    LLog.i("MinioUploadTask failed ${response.code}")
                    notify(0, LoadListener.Failed)
                }
            }
        })
    }

    override fun cancel() {
        call?.let {
            if (!it.isCanceled()) {
                it.cancel()
            }
        }
        call = null
        running.set(false)
    }

    override fun notify(progress: Int, state: Int) {
        val url = if (keyUrl != null) {
            keyUrl!!
        } else {
            key
        }
        fileLoaderModule.notifyListeners(taskId, progress, state, url, path)
    }
}