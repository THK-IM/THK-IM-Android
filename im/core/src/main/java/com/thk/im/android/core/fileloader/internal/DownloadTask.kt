package com.thk.im.android.core.fileloader.internal

import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.StringUtils
import com.thk.im.android.core.exception.HttpStatusCodeException
import com.thk.im.android.core.exception.UnknownException
import com.thk.im.android.core.fileloader.FileLoadState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class DownloadTask(
    private val url: String,
    private val downloadParam: String,
    private val fileLoaderModule: DefaultFileLoadModule
) : LoadTask(url) {

    private val tag = "DownloadTask"
    private var running = AtomicBoolean(true)
    private val localFile: File
    private val localTmpFile: File
    private var call: Call? = null

    init {
        val hashUrl = StringUtils.shaEncrypt(url)
        localFile = File(fileLoaderModule.cacheDir, hashUrl)
        localTmpFile = File(fileLoaderModule.cacheDir, "${hashUrl}.tmp")
        notify(0, FileLoadState.Wait.value)
    }

    override fun start() {
        notify(0, FileLoadState.Init.value)
        if (localFile.exists()) {
            notify(100, FileLoadState.Success.value)
            return
        }
        val requestBuilder = Request.Builder()
        if (url.startsWith("http", true)) {
            requestBuilder.url(url)
        } else {
            requestBuilder.addHeader("Token", fileLoaderModule.token)
            requestBuilder.url("${fileLoaderModule.endpoint}/session/object/download_url?${downloadParam}")
        }
        call = fileLoaderModule.okHttpClient.newCall(requestBuilder.build())
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, FileLoadState.Failed.value, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code !in 200..299) {
                    response.close()
                    notify(0, FileLoadState.Failed.value, UnknownException("http success but body is null"))
                    return
                }
                if (response.body == null) {
                    response.close()
                    notify(0, FileLoadState.Failed.value, FileNotFoundException())
                    return
                }
                notify(0, FileLoadState.Ing.value)
                // 如果文件存在 删除然后创建新文件
                if (localTmpFile.exists()) {
                    if (!localTmpFile.delete()) {
                        response.close()
                        notify(
                            0,
                            FileLoadState.Failed.value,
                            FileSystemException(localTmpFile, null, "create Failed")
                        )
                        return
                    }
                }
                if (!localTmpFile.createNewFile()) {
                    response.close()
                    notify(
                        0,
                        FileLoadState.Failed.value,
                        FileSystemException(localTmpFile, null, "create Failed")
                    )
                    return
                }
                val fos = FileOutputStream(localTmpFile)
                val inputStream = response.body!!.byteStream()
                // 储存下载文件的目录
                try {
                    val buf = ByteArray(256 * 1024)
                    val total = response.body!!.contentLength()
                    var sum = 0L
                    while (running.get()) {
                        val len = inputStream.read(buf)
                        if (len != -1) {
                            fos.write(buf, 0, len)
                            sum += len
                            val progress = (sum * 1.0f / total * 100).toInt()
                            notify(progress, FileLoadState.Ing.value)
                        } else {
                            break
                        }
                    }
                    fos.flush()
                    LLog.d("sum: $sum, total: $total")
                    if (sum == total) {
                        localTmpFile.renameTo(localFile)
                        notify(100, FileLoadState.Success.value)
                    } else {
                        localTmpFile.delete()
                        notify(
                            (sum * 1.0f / total * 100).toInt(),
                            FileLoadState.Failed.value,
                            FileNotFoundException()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    notify(0, FileLoadState.Failed.value, e)
                } finally {
                    fos.close()
                    inputStream.close()
                    response.close()
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
    }

    override fun notify(progress: Int, state: Int, exception: Exception?) {
        val path = if (localFile.exists()) {
            localFile.absolutePath
        } else {
            ""
        }
        LLog.v(tag, "$taskId, $progress, $state")
        fileLoaderModule.notifyListeners(progress, state, url, path, exception)
    }

}