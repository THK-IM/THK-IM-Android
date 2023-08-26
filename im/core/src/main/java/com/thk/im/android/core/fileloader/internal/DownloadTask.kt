package com.thk.im.android.core.fileloader.internal

import com.thk.im.android.base.LLog
import com.thk.im.android.core.fileloader.LoadListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


class DownloadTask(
    private val url: String,
    private val path: String,
    taskId: String,
    private val fileLoaderModule: DefaultFileLoaderModule
) : FileTask(taskId) {

    private val tag = "DownloadTask"
    private var running = AtomicBoolean(true)
    private val loadingPath = "$path.tmp"

    init {
        notify(0, LoadListener.Wait)
    }

    override fun start() {
        notify(0, LoadListener.Init)
        val okHttpClient = fileLoaderModule.downloadClient
        val request = Request.Builder()
            .addHeader("token", fileLoaderModule.token)
            //访问路径
            .url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                notify(0, LoadListener.Failed)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code !in 200..299) {
                    response.close()
                    notify(0, LoadListener.Failed)
                    return
                }
                if (response.body == null) {
                    response.close()
                    notify(0, LoadListener.Failed)
                    return
                }
                notify(0, LoadListener.Ing)
                val inputStream = response.body!!.byteStream()
                val file = File(loadingPath)
                // 如果文件存在 删除然后创建新文件
                if (file.exists()) {
                    if (!file.delete()) {
                        response.close()
                        notify(0, LoadListener.Failed)
                        return
                    }
                }
                if (!file.createNewFile()) {
                    response.close()
                    notify(0, LoadListener.Failed)
                    return
                }
                val fos = FileOutputStream(file)
                // 储存下载文件的目录
                try {
                    val buf = ByteArray(2048)
                    val total = response.body!!.contentLength()
                    var sum = 0L
                    while (running.get()) {
                        val len = inputStream.read(buf)
                        if (len != -1) {
                            fos.write(buf, 0, len)
                            sum += len
                            val progress = (sum * 1.0f / total * 100).toInt()
                            notify(progress, LoadListener.Ing)
                        } else {
                            break
                        }
                    }
                    fos.flush()
                    LLog.v("sum: $sum, total: $total")
                    if (sum == total) {
                        notify(100, LoadListener.Success)
                    } else {
                        notify((sum * 1.0f / total * 100).toInt(), LoadListener.Failed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    notify(0, LoadListener.Failed)
                } finally {
                    fos.close()
                    inputStream.close()
                    response.close()
                }
            }
        })

    }

    override fun cancel() {
        running.set(false)
    }

    override fun notify(progress: Int, state: Int) {
        LLog.v(tag, "$taskId, $progress, $state")
        // 下载成功时把文件拷贝到最终路径上
        if (state == LoadListener.Success) {
            val loadingFile = File(loadingPath)
            val file = File(path)
            if (file.exists()) {
                if (!file.delete()) {
                    fileLoaderModule.notifyListeners(
                        taskId,
                        progress,
                        LoadListener.Failed,
                        url,
                        path
                    )
                    return
                }
            }
            if (!loadingFile.renameTo(file)) {
                fileLoaderModule.notifyListeners(taskId, progress, LoadListener.Failed, url, path)
                return
            }
        }
        fileLoaderModule.notifyListeners(taskId, progress, state, url, path)
    }

}