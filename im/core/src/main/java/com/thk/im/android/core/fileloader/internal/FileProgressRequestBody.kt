package com.thk.im.android.core.fileloader.internal

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLConnection
import kotlin.math.min


class FileProgressRequestBody(
    private var file: File, private var listener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun transferred(size: Long, progress: Int)
    }

    override fun contentLength(): Long {
        return file.length()
    }

    /**
     * Returns the Content-Type header for this body.
     */
    override fun contentType(): MediaType {
        return URLConnection.guessContentTypeFromName(file.name).toMediaType()
    }

    /**
     * Writes the content of this request to `sink`.
     *
     * @param sink
     */
    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val source: Source = FileInputStream(file).source()
        source.use {
            var total: Long = 0
            var count: Long
            val readCount = min(contentLength() / 10, SEGMENT_SIZE)
            while (it.read(sink.buffer, readCount).also { size ->
                    count = size
                } != -1L) {
                total += count
                val progress = (total * 100 / contentLength()).toInt()
                listener.transferred(total, progress)
            }
            source.close()
        }
        sink.flush()
    }

    companion object {
        const val SEGMENT_SIZE = 256 * 1024L // okio.Segment.SIZE
    }
}
