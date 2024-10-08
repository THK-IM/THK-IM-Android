package com.thk.im.android.core.glide

import android.os.Build
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.data.DataFetcher.DataCallback
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.util.ContentLengthInputStream
import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Synthetic
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.io.InputStream


class OkHttpStreamFetcher(private val client: Call.Factory, private val url: GlideUrl) :
    DataFetcher<InputStream>,
    Callback {

    @Synthetic
    var stream: InputStream? = null

    @Synthetic
    var responseBody: ResponseBody? = null

    @Volatile
    private var call: Call? = null
    private var callback: DataCallback<in InputStream>? = null

    override fun onFailure(call: Call, e: IOException) {
        callback?.onLoadFailed(e)
    }

    @Throws(IOException::class)
    override fun onResponse(call: Call, response: Response) {
        responseBody = response.body
        if (response.isSuccessful) {
            val contentLength: Long = Preconditions.checkNotNull(responseBody).contentLength()
            stream = ContentLengthInputStream.obtain(responseBody!!.byteStream(), contentLength)
            callback?.onDataReady(stream)
        } else {
            callback?.onLoadFailed(HttpException(response.message, response.code))
        }
    }

    override fun loadData(priority: Priority, callback: DataCallback<in InputStream>) {
        val requestBuilder: Request.Builder = Request.Builder().url(url.toStringUrl())
        for ((key, value) in url.headers) {
            requestBuilder.addHeader(key, value)
        }
        val request: Request = requestBuilder.build()
        this.callback = callback
        val call = client.newCall(request) ?: return
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            call.enqueue(this)
        } else {
            try {
                // Calling execute instead of enqueue is a workaround for #2355, where okhttp throws a
                // ClassCastException on O.
                onResponse(call, call.execute())
            } catch (e: IOException) {
                onFailure(call, e)
            } catch (e: ClassCastException) {
                // It's not clear that this catch is necessary, the error may only occur even on O if
                // enqueue is used.
                onFailure(call, IOException("Workaround for framework bug on O", e))
            }
        }
        this.call = call
    }

    override fun cleanup() {
        try {
            if (stream != null) {
                stream!!.close()
            }
        } catch (e: IOException) {
            // Ignored
        }
        if (responseBody != null) {
            responseBody!!.close()
        }
        callback = null
    }

    override fun cancel() {
        call?.cancel()
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

    companion object {
        private const val TAG = "OkHttpFetcher"
    }
}
