package com.thk.im.android.core.base.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import pl.droidsonroids.gif.GifDrawable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.sqrt


object CompressUtils {

    fun compress(srcPath: String, size: Int, desPath: String): Flowable<String> {
        return Flowable.create({
            try {
                compressSync(srcPath, size, desPath)
                it.onNext(desPath)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }, BackpressureStrategy.LATEST)
    }

    @Throws(Exception::class)
    fun compressSync(srcPath: String, size: Int, desPath: String) {
        val length = File(srcPath).length()
        if (length <= size) {
            val success = copyFile(srcPath, desPath)
            if (success) {
                return
            } else {
                throw IOException()
            }
        }
        val isGif = isGif(srcPath)
        if (isGif) {
            val desFile = File(desPath)
            if (desFile.exists()) {
                desFile.delete()
            }
            val success = desFile.createNewFile()
            if (!success) {
                throw IOException()
            }
            val gifDrawable = GifDrawable(srcPath)
            val rate = sqrt((length / size).toDouble()).toInt() * 2
            var sample = 2
            var mod = 1
            while (sample * mod < rate) {
                if (gifDrawable.minimumWidth / sample > 100) {
                    sample *= 2
                } else {
                    mod *= 2
                }
            }
            compressFrameGif(
                gifDrawable, mod, sample, desPath
            )
            gifDrawable.recycle()
        } else {
            val rate = sqrt((length / size).toDouble()).toInt() * 2
            var sample = 2
            while (sample < rate) {
                sample *= 2
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = sample

            val tagBitmap = BitmapFactory.decodeFile(srcPath, options)
            val stream = ByteArrayOutputStream()

            tagBitmap!!.compress(
                if (tagBitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                if (tagBitmap.hasAlpha()) 100 else 60,
                stream
            )
            tagBitmap.recycle()
            val desFile = File(desPath)
            if (desFile.exists()) {
                desFile.delete()
            }
            val success = desFile.createNewFile()
            if (!success) {
                stream.close()
                throw IOException()
            }
            val fos = FileOutputStream(desPath)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
            stream.close()
        }
    }

    @Throws(Exception::class)
    fun compressSync(source: Bitmap, size: Int, desPath: String) {
        val pair = getPathsFromFullPath(desPath)
        val dirFile = File(pair.first)
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw IOException("create dir $desPath failed")
            }
        }
        val file = File(desPath)
        if (file.exists()) {
            if (!file.delete()) {
                throw IOException("delete file ${file.absolutePath} failed")
            }
        }
        if (!file.createNewFile()) {
            throw IOException("create file ${file.absolutePath} failed")
        }
        val length = source.byteCount
        if (length <= size) {
            val outputFileStream = FileOutputStream(desPath)
            if (source.hasAlpha()) {
                source.compress(Bitmap.CompressFormat.PNG, 100, outputFileStream)
            } else {
                source.compress(Bitmap.CompressFormat.JPEG, 100, outputFileStream)
            }
            outputFileStream.flush()
            outputFileStream.close()
        }
        val rate = sqrt((length / size).toDouble()).toInt() * 2
        var sample = 2
        while (sample < rate) {
            sample *= 2
        }
        val stream = ByteArrayOutputStream()

        val width: Int = source.width
        val height: Int = source.height
        val scale = sqrt(sample.toDouble()).toFloat()
        val m = Matrix()
        m.setScale(1 / scale, 1 / scale)
        val bitmap = Bitmap.createBitmap(
            source, 0, 0, width, height, m, true
        )
        bitmap.compress(
            if (bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
            if (bitmap.hasAlpha()) 100 else 60,
            stream
        )
        bitmap.recycle()
        val desFile = File(desPath)
        if (desFile.exists()) {
            desFile.delete()
        }
        val success = desFile.createNewFile()
        if (!success) {
            stream.close()
            throw IOException()
        }
        val fos = FileOutputStream(desPath)
        fos.write(stream.toByteArray())
        fos.flush()
        fos.close()
        stream.close()
    }


    /**
     * 获取bitmap的长宽
     */
    fun getBitmapAspect(path: String): Pair<Int, Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        return Pair(options.outWidth, options.outHeight)
    }


    /**
     * 获取视频封面
     */
    fun getVideoParams(filePath: String, frameTime: Long = 1000): Pair<Bitmap, Int>? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val b = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: return null
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString()
                    .toInt() / 1000 // 时长(秒)
            return Pair(b, duration)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun isGif(localPath: String): Boolean {
        try {
            val inputStream = FileInputStream(localPath)
            val flags = IntArray(5)
            flags[0] = inputStream.read()
            flags[1] = inputStream.read()
            flags[2] = inputStream.read()
            flags[3] = inputStream.read()
            inputStream.skip((inputStream.available() - 1).toLong())
            flags[4] = inputStream.read()
            inputStream.close()
            return flags[0] == 71 && flags[1] == 73 && flags[2] == 70 && flags[3] == 56 && flags[4] == 0x3B
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(Exception::class)
    private fun copyFile(srcPath: String, desPath: String): Boolean {
        var input: InputStream? = null
        var output: OutputStream? = null
        var res = false
        try {
            val pair = getPathsFromFullPath(desPath)
            val dirFile = File(pair.first)
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    throw IOException("create dir $desPath failed")
                }
            }
            val file = File(desPath)
            if (file.exists()) {
                if (!file.delete()) {
                    throw IOException("delete file ${file.absolutePath} failed")
                }
            }
            if (!file.createNewFile()) {
                throw IOException("create file ${file.absolutePath} failed")
            }
            input = FileInputStream(File(srcPath))
            output = FileOutputStream(File(desPath))
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buf).also { bytesRead = it } > 0) {
                output.write(buf, 0, bytesRead)
            }
            res = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
            output?.close()
        }
        return res
    }


    private fun getPathsFromFullPath(fullPath: String): Pair<String, String> {
        val i = fullPath.lastIndexOf("/")
        return if (i <= 0 || i >= fullPath.length) {
            Pair("", fullPath)
        } else {
            Pair(fullPath.substring(0, i), fullPath.substring(i + 1))
        }
    }

    @Throws(Exception::class)
    private fun compressFrameGif(
        gifDrawable: GifDrawable, mod: Int, sample: Int, savePath: String
    ) {
        var bos: ByteArrayOutputStream? = null
        var outStream: FileOutputStream? = null
        try {
            val gifFrames = gifDrawable.numberOfFrames
            val gifDuration = gifDrawable.duration
            bos = ByteArrayOutputStream()
            val encoder = AnimatedGifEncoder()
            encoder.setRepeat(0)
            encoder.start(bos)
            for (i in 0 until gifFrames) {
                if (i % mod == 0) {
                    val source = gifDrawable.seekToFrameAndGet(i)
                    val scale = sample.toFloat()
                    val m = Matrix()
                    m.setScale(1 / scale, 1 / scale)
                    val bitmap = Bitmap.createBitmap(
                        source, 0, 0, source.width, source.height, m, true
                    )
                    encoder.addFrame(bitmap)
                    encoder.setDelay(gifDuration / (gifFrames / mod))
                }
            }
            encoder.finish()
            outStream = FileOutputStream(savePath)
            outStream.write(bos.toByteArray())
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                bos?.close()
                outStream?.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}