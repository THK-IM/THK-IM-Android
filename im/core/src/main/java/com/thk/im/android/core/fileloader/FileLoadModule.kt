package com.thk.im.android.core.fileloader

interface FileLoadModule {


    /**
     * 获取任务id
     * @param key 任务key
     * @param path 本地路径
     * @param type 类型 "Upload" or "Download"
     * @return 任务id
     */
    fun getTaskId(key: String, path: String, type: String): String


    /**
     * 获取上传key
     * @param sId session id
     * @param uId 用户id
     * @param fileName 文件命名
     * @param msgClientId 客户端消息id
     * @return key
     */
    fun getUploadKey(sId: Long, uId: Long, fileName: String, msgClientId: Long): String

    /**
     * 从上传key中解析出 sId, uId, fileName
     */
    fun parserUploadKey(key: String): Triple<Long, Long, String>?

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