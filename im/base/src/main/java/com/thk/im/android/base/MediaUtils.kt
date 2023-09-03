package com.thk.im.android.base

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.roundToInt

object MediaUtils {

    fun compress(app: Application, src: String, size: Int): Flowable<String> {
        return Flowable.create({
            Luban.with(app)
                .ignoreBy(size)
                .setFocusAlpha(true).load(src)
                .setCompressListener(object : OnCompressListener {
                    override fun onStart() {
                        LLog.d("compress onStart $src")
                    }

                    override fun onSuccess(index: Int, compressFile: File?) {
                        if (compressFile != null) {
                            it.onNext(compressFile.absolutePath)
                        } else {
                            it.onError(FileNotFoundException())
                        }
                    }

                    override fun onError(index: Int, e: Throwable) {
                        LLog.e("compress onStart $src $e")
                        it.onError(e)
                    }

                })
                .launch()
        }, BackpressureStrategy.LATEST)
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
    fun getVideoParams(filePath: String, frameTime: Long = 1000): Pair<Bitmap, Int>? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val b = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: return null
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toString().toInt() / 1000 + 1 // 时长(秒)
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