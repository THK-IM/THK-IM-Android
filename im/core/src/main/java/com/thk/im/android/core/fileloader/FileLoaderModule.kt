package com.thk.im.android.core.fileloader

interface FileLoaderModule {

    /**
     *  下载
     * @param url 网路地址
     * @param path 本地路径
     * @param listener 进度监听器
     * @return 任务id
     */
    fun download(url: String, path: String, listener: LoadListener): String

    /**
     *  上传
     * @param key 对象存储的key
     * @param path 本地路径
     * @param listener 进度监听器
     * @return 任务id
     */
    fun upload(key: String, path: String, listener: LoadListener): String

    /**
     * 取消下载
     */
    fun cancelDownload(taskId: String)

    /**
     * 取消下载监听
     */
    fun cancelDownloadListener(taskId: String)

    /**
     * 取消上传
     */
    fun cancelUpload(taskId: String)

    /**
     * 取消上传监听
     */
    fun cancelUploadListener(taskId: String)

}