package com.thk.im.android.base

import android.content.res.Resources
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
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

    @JvmStatic
    fun displayCornerImageByPath(imageView: ImageView, path: String, cornerDp: Int = 5) {
        val file = File(path)
        if (file.exists()) {
            val corner = dp2px(cornerDp).toFloat()
            val roundedCorners = GranularRoundedCorners(corner, corner, corner, corner)
            val isGif = CompressUtils.isGif(path)
            if (isGif) {
                Glide.with(imageView.context.applicationContext).asGif().load(File(path))
                    .apply(RequestOptions().transform(roundedCorners)).into(imageView)
            } else {
                Glide.with(imageView.context.applicationContext).asBitmap().load(File(path))
                    .apply(RequestOptions().transform(roundedCorners)).into(imageView)
            }
        }
    }

    private fun dp2px(dp: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (scale * dp).toInt()
    }
}