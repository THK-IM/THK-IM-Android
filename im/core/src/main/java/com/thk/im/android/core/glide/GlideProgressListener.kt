package com.thk.im.android.core.glide

interface GlideProgressListener {

    /**
     * 图片加载进度回调
     */
    fun onLoadProgress(url: String, isDone: Boolean, progress: Int)

}