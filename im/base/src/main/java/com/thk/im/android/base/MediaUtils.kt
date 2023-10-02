package com.thk.im.android.base

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt


object MediaUtils {

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
        val options = BitmapFactory.Options()
        val rate = sqrt((length / size).toDouble()).toInt() * 2
        var sample = 2
        while (sample < rate) {
            sample *= 2
        }
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
        val m = Matrix()

        val width: Int = source.width
        val height: Int = source.height
        val scale = sqrt(sample.toDouble()).toFloat()
        m.setScale(scale, scale)
        val bitmap = Bitmap.createBitmap(
            source,
            0,
            0,
            (width / scale).toInt(),
            (height / scale).toInt(),
            m,
            true
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

    private fun computeSize(width: Int, height: Int): Int {
        val srcWidth = if (width % 2 == 1) width + 1 else width
        val srcHeight = if (height % 2 == 1) height + 1 else height
        val longSide: Int = max(srcWidth, srcHeight)
        val shortSide: Int = min(srcWidth, srcHeight)
        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            if (longSide < 600) {
                1
            } else if (longSide < 1200) {
                2
            } else if (longSide in 1200..2400) {
                4
            } else {
                longSide / 600
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 600 == 0) 1 else longSide / 600
        } else {
            ceil(longSide / (600.0 / scale)).toInt()
        }
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

    fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 2
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    fun rotatingImageView(angle: Int, bitmap: Bitmap?): Bitmap? {
        if (null == bitmap) {
            return null
        }
        // 旋转图片 动作
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        // 创建新的图片
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
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
                    .toInt() / 1000 + 1 // 时长(秒)
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


}