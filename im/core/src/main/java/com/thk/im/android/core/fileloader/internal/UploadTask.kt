package com.thk.im.android.core.fileloader.internal

import com.google.gson.Gson
import com.thk.im.android.core.exception.CodeMessage
import com.thk.im.android.core.exception.HttpException
import com.thk.im.android.core.fileloader.FileLoadState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


class UploadTask(
    private val path: String,
    private val uploadParams: String,
    private val fileLoaderModule: DefaultFileLoadModule
) : LoadTask(path) {

    private var running = AtomicBoolean(true)
    private var uploadCall: Call? = null
    private var getParamsCall: Call? = null
    private var keyUrl: String? = null

    init {
        notify(0, FileLoadState.Wait.value)
    }

    override fun start() {
        // first 调用api获取上传参数
        val url =
            "${fileLoaderModule.endpoint}/session/object/upload_params?" + uploadParams
        val request = Request.Builder().url(url).build()
        getParamsCall = fileLoaderModule.okHttpClient.newCall(request)
        getParamsCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, FileLoadState.Failed.value, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    if (response.body == null) {
                        val codeMessage = CodeMessage(response.code, "unknown")
                        notify(0, FileLoadState.Failed.value, HttpException(codeMessage))
                    } else {
                        try {
                            val json = response.body?.string()
                            val uploadParams = Gson().fromJson(json, UploadParams::class.java)
                            startUpload(uploadParams)
                        } catch (e: Exception) {
                            val codeMessage = CodeMessage(
                                500,
                                if (e.message == null) "internal server error" else e.message!!
                            )
                            notify(0, FileLoadState.Failed.value, HttpException(codeMessage))
                        }
                    }
                } else {
                    val msg = response.body?.string() ?: "unknown"
                    val codeMessage = CodeMessage(response.code, msg)
                    notify(0, FileLoadState.Failed.value, HttpException(codeMessage))
                }
            }
        })
    }

    private fun startUpload(uploadParams: UploadParams) {
        if (!running.get()) {
            notify(0, FileLoadState.Failed.value, null)
            return
        }
        val file = File(path)
        if (file.length() <= 0) {
            notify(0, FileLoadState.Failed.value, FileNotFoundException())
            return
        }
        notify(0, FileLoadState.Ing.value)
        val requestBodyBuilder = MultipartBody.Builder()
        requestBodyBuilder.setType(MultipartBody.FORM)
        for ((k, v) in uploadParams.params) {
            requestBodyBuilder.addFormDataPart(k, v)
        }
        val fileBody: RequestBody =
            FileProgressRequestBody(file, object : FileProgressRequestBody.ProgressListener {
                override fun transferred(size: Long, progress: Int) {
                    notify(progress, FileLoadState.Ing.value, null)
                }
            })
        requestBodyBuilder.addFormDataPart("file", file.name, fileBody)
        val request = Request.Builder().method(uploadParams.method, requestBodyBuilder.build())
            .url(uploadParams.url).build()
        uploadCall = fileLoaderModule.okHttpClient.newCall(request)
        uploadCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, FileLoadState.Failed.value, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    keyUrl = "${uploadParams.id}"
                    notify(100, FileLoadState.Success.value, null)
                } else {
                    val msg = response.body?.string() ?: "unknown"
                    val codeMessage = CodeMessage(response.code, msg)
                    notify(0, FileLoadState.Failed.value, HttpException(codeMessage))
                }
            }
        })
    }

    override fun cancel() {
        uploadCall?.let {
            if (!it.isCanceled()) {
                it.cancel()
            }
        }
        uploadCall = null
        getParamsCall?.let {
            if (!it.isCanceled()) {
                it.cancel()
            }
        }
        getParamsCall = null
        running.set(false)
    }

    override fun notify(progress: Int, state: Int, exception: Exception?) {
        val url = if (keyUrl != null) {
            keyUrl!!
        } else {
            ""
        }
        fileLoaderModule.notifyListeners(progress, state, url, path, exception)
    }
}