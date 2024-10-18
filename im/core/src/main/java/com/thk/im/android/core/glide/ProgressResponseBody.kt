package com.thk.im.android.core.glide

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException


class ProgressResponseBody(private val url: String, private val responseBody: ResponseBody) :
    ResponseBody() {

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        return ProgressSource(responseBody.source()).buffer()
    }

    private inner class ProgressSource(source: Source) : ForwardingSource(source) {

        var totalBytesRead: Long = 0
        var progress = 0

        @Throws(IOException::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            if (bytesRead > 0) {
                val fullLength = responseBody.contentLength()
                totalBytesRead += bytesRead
                val progress = (100 * totalBytesRead / fullLength).toInt()
                if (this.progress != progress) {
                    this.progress = progress
                    val listener = GuildProgressInterceptor.LISTENER_MAP[url]?.get()
                    Log.v(
                        "LoadProgress",
                        "${url} ${totalBytesRead} ${fullLength} ${progress} ${listener == null}"
                    )
                    listener?.onLoadProgress(url, progress == 100, progress)
                }
            }
            return bytesRead
        }
    }
}
