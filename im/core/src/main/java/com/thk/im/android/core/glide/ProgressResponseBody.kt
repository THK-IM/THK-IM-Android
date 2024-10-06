package com.thk.im.android.core.glide

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
        var currentProgress: Int = 0

        @Throws(IOException::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            val fullLength = responseBody.contentLength()
            if (bytesRead == -1L) {
                totalBytesRead = fullLength
            } else {
                totalBytesRead += bytesRead
            }
            val progress = (100f * totalBytesRead / fullLength).toInt()
            GuildProgressInterceptor.LISTENER_MAP[url]?.get()?.let {
                if (progress != currentProgress) {
                    it.onLoadProgress(url, progress == 100, progress)
                }
            }
            currentProgress = progress
            return bytesRead
        }
    }
}
