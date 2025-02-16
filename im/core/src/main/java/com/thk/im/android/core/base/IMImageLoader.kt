package com.thk.im.android.core.base

import android.content.Context
import android.content.res.Resources
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.thk.im.android.core.base.utils.CompressUtils
import io.reactivex.Flowable
import io.reactivex.subscribers.DisposableSubscriber
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

    fun displayImageUrl(imageView: ImageView, url: String, placeholder: Int) {
        Glide.with(imageView.context.applicationContext).load(url).placeholder(placeholder).into(imageView)
    }

    fun displayDoNotAnimate(imageView: ImageView, url: String) {
        Glide.with(imageView.context.applicationContext).load(url).dontAnimate()
            .placeholder(imageView.getDrawable())
            .into(imageView)
    }

    fun displayDoNotAnimateByPath(imageView: ImageView, path: String) {
        Glide.with(imageView.context.applicationContext).load(File(path)).dontAnimate()
            .placeholder(imageView.getDrawable())
            .into(imageView)
    }

    fun preloadFile(
        imageView: ImageView,
        url: String,
        listener: (url: String, file: File?, e: Exception?) -> Unit
    ) {
        Glide.with(imageView.context).asFile().load(url)
            .addListener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>,
                    isFirstResource: Boolean
                ): Boolean {
                    listener(url, null, e)
                    return false
                }

                override fun onResourceReady(
                    resource: File,
                    model: Any,
                    target: Target<File>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    listener(url, resource, null)
                    return true
                }
            }).preload()
    }

    fun isCacheExisted(ctx: Context, url: String, listener: (path: String?) -> Unit) {
        Flowable.just(false).flatMap {
            val file: File? = try {
                Glide.with(ctx).downloadOnly().load(url)
                    .apply(RequestOptions().onlyRetrieveFromCache(true)).submit().get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            Flowable.just((file?.absolutePath))
        }.compose(RxTransform.flowableToMain())
            .subscribe(object : DisposableSubscriber<String?>() {
                override fun onNext(t: String?) {
                    listener.invoke(t)
                }

                override fun onError(t: Throwable?) {
                    dispose()
                    listener.invoke(null)
                }

                override fun onComplete() {
                    dispose()
                }
            })
    }


    private fun dp2px(dp: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (scale * dp).toInt()
    }
}