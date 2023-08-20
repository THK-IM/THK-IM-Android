package com.thk.im.android.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import java.io.*
import kotlin.math.roundToInt

object MediaUtils {

    fun compass(pathUrl: String): Bitmap? {
        if (!File(pathUrl).exists()) {
            return null
        }
        var bufferedInputStream: BufferedInputStream? = null
        try {
            val degree: Int = readPictureDegree(pathUrl)
            bufferedInputStream = BufferedInputStream(FileInputStream(File(pathUrl)))
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(bufferedInputStream, null, options)
            options.inSampleSize = calculateInSampleSize(options, 400, 600)
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options)
            return rotatingImageView(degree, bitmap)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            bufferedInputStream?.close()
        }
        return null
    }

    /**
     * 获取bitmap的长宽
     */
    fun getBitmapAspect(path: String): Pair<Int, Int> {
        val inputStream = FileInputStream(File(path))
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        return Pair(options.outWidth, options.outHeight)
    }

    fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
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
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
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
            bitmap, 0, 0,
            bitmap.width, bitmap.height, matrix, true
        )
    }


    /**
     * 获取视频封面
     */
    fun getVideoParams(filePath: String?, frameTime: Long = 1000): Pair<Bitmap, Long>? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val b = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: return null
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toString()
                    .toLong() / 1000 //时长(秒)
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


}