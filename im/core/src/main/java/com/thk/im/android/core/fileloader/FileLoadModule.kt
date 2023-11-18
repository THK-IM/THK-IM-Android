package com.thk.im.android.core.fileloader

import com.thk.im.android.core.db.entity.Message

interface FileLoadModule {

    /**
     *  下载
     * @param key 文件id或url
     * @param message 消息
     * @param listener 进度监听器
     */
    fun download(key: String, message: Message, listener: LoadListener)
    

    /**
     *  上传
     * @param path 本地路径
     * @param message 消息
     * @param listener 进度监听器
     * @return 任务id
     */
    fun upload(path: String, message: Message, listener: LoadListener)

    /**
     * 取消下载
     */
    fun cancelDownload(url: String)

    /**
     * 取消下载监听
     */
    fun cancelDownloadListener(url: String)

    /**
     * 取消上传
     */
    fun cancelUpload(path: String)

    /**
     * 取消上传监听
     */
    fun cancelUploadListener(path: String)

}