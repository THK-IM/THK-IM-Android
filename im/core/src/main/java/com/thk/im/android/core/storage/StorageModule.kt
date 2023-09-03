package com.thk.im.android.core.storage

import android.graphics.Bitmap
import java.io.File
import java.io.IOException

interface StorageModule {

    /**
     * 获取文件目录和文件名
     */
    fun getPathsFromFullPath(fullPath: String): Pair<String, String>

    /**
     * 从path中获取文件扩展名
     */
    fun getFileExt(path: String): Pair<String, String>

    @Throws(IOException::class)
    fun saveImageInto(fullPath: String, b: Bitmap)

    @Throws(IOException::class)
    fun saveInto(fullPath: String, b: ByteArray)

    @Throws(IOException::class)
    fun saveInto(dir: String, name: String, b: ByteArray)

    /**
     * 拷贝文件
     */
    fun copyFile(srcPath :String, desPath: String) :Boolean

    /**
     * 申请存放用户头像的地址
     */
    fun allocAvatarPath(id: Long, avatarUrl: String, type: Int): String


    /**
     * 申请本地文件路径
     */
    fun allocSessionFilePath(
        sid: Long,
        fileName: String,
        format: String
    ): String

    /**
     * 是否为IM内部的路径
     */
    fun isAssignedPath(
        path: String,
        format: String,
        sid: Long
    ): Boolean

    /**
     * 获取session
     */
    fun getSessionCacheFiles(format: String, sid: Long): List<File>

    /**
     * 获取session路径
     */
    fun getSessionCacheSize(sid: Long): Long
}