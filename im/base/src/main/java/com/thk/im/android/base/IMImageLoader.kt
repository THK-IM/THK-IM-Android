package com.thk.im.android.base

import android.content.res.Resources
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.thk.im.android.base.utils.CompressUtils
import java.io.File

object IMImageLoader {

    fun displayImageByPath(imageView: ImageView, path: String) {
        val file = File(path)
        if (file.exists()) {
            val isGif = CompressUtils.isGif(path)
            if (isGif) {
                Glide.with(imageView.context.applicationContext).asGif().load(File(path))
                    .into(imageView)
            } else {
                Glide.with(imageView.context.applicationContext).asBitmap().load(File(path))
                    .into(imageView)
            }
        }
    }

    fun displayImageUrl(imageView: ImageView, url: String) {
        Glide.with(imageView.context.applicationContext).load(url).into(imageView)
    }


    private fun dp2px(dp: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (scale * dp).toInt()
    }
}