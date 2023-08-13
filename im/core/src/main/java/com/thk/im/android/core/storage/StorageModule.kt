package com.thk.im.android.core.storage

import android.graphics.Bitmap
import java.io.File
import java.io.IOException

interface StorageModule {

    /**
     * 获取文件目录和文件名
     */
    fun getPathsFromFullPath(fullPath: String): Pair<String, String>?

    /**
     * 从url中获取文件扩展名
     */
    fun getFileExtFromUrl(url: String): String

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
     * 申请会话下文件存放路径，函数内不会创建文件
     * @return   pair  第一个结果是文件保存的路径 第二个结果是阿里云上传时的key
     * /{application}/{files}/im/{uid}/session-${sid}/{format}/xxx.jpeg
     * 文件名重复返回 /{application}/{files}/im/{uid}/session-${sid}/{format}/xxx.1.jpeg
     * @param sid 会话id
     * @param uid 用户id
     * @param fileName 文件名 dsfds.jpeg
     * @param format 文件类型，img(包含png/jpeg/gif等格式)/video(spx)/voice/file(包含doc/ppt/txt/等格式)
     */
    @Throws(IOException::class)
    fun allocSessionFilePath(
        sid: Long,
        uid: Long,
        fileName: String,
        format: String
    ): Pair<String, String>


    /**
     * 申请文件服务器上传key
     */
    fun allocServerFilePath(
        sid: Long,
        uid: Long,
        fileName: String
    ): String

    /**
     * 申请本地文件路径
     */
    fun allocLocalFilePath(
        sid: Long,
        fileName: String,
        format: String
    ): String

    /**
     * 是否为IM内部的路径
     */
    fun isAssignedPath(
        path: String,
        fileName: String,
        format: String, sid: Long
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