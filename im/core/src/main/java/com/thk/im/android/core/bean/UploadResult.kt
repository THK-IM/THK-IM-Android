package com.thk.im.android.core.bean


/**
 * 上传结果
 *
 * @id 消息id
 * @status 状态:0上传中 1上传成功 2上传失败
 */
data class UploadResult(
    val id: Long,
    val status: UploadStatus,
    val progress: Int,
    val result: String
)