package com.thk.im.preview.viewholder

import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import com.danikula.videocache.CacheListener
import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.preview.R
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import com.thk.im.preview.VideoCache
import com.thk.im.preview.player.THKVideoPlayerView
import io.reactivex.Flowable
import java.io.File


class VideoPreviewVH(liftOwner: LifecycleOwner, itemView: View) :
    PreviewVH(liftOwner, itemView), CacheListener {

    private val lyVideoParent = itemView.findViewById<RelativeLayout>(R.id.ly_video_parent)
    private val ivCover = itemView.findViewById<AppCompatImageView>(R.id.iv_image)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        this.message?.let {
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMVideoMsgData::class.java)
                if (data != null) {
                    if (data.thumbnailPath != null) {
                        IMImageLoader.displayImageByPath(ivCover, data.thumbnailPath!!)
                    } else {
                        downloadCover()
                    }
                }
            }
        }
    }

    private fun downloadCover() {
        message?.let {
            if (it.content != null) {
                try {
                    val content = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                    if (content != null) {
                        IMCoreManager.messageModule.getMsgProcessor(it.type)
                            .downloadMsgContent(it, IMMsgResourceType.Thumbnail.value)
                    }
                } catch (e: Exception) {
                    LLog.e("${it.content} ${e.toString()}")
                }
            }
        }
    }

    override fun startPreview(playerView: THKVideoPlayerView) {
        message?.let {
            var played = false
            playerView.hideBottomControllers(false)
            playerView.attachToParent(lyVideoParent)
            if (it.data != null) {
                val data = Gson().fromJson(it.data, IMVideoMsgData::class.java)
                if (data?.path != null) {
                    played = true
                    playerView.playWithUrl(data.path!!)
                }
            }

            if (!played) {
                if (it.content != null) {
                    val body = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                    if (body?.url != null) {
                        val realUrl = getRealUrl(body.url!!)
                        val cachePath = VideoCache.getCachePath(realUrl)
                        if (cachePath == null) {
                            VideoCache.registerCacheListener(this, realUrl)
                            playerView.playWithUrl(realUrl)
                        } else {
                            updateDb(it, File(cachePath), realUrl)
                            playerView.playWithUrl(cachePath)
                        }
                    }
                }
            }
        }
    }

    private fun getRealUrl(url: String): String {
        var realUrl = url
        if (!url.startsWith("http")) {
            realUrl = "${IMCoreManager.imApi.endpoint()}/session/object/download_url?id=${url}"
        }
        return realUrl
    }


    override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
        LLog.d("onCacheAvailable $percentsAvailable")
        if (percentsAvailable < 100) {
            return
        }
        if (cacheFile == null || url == null || !cacheFile.exists() || !cacheFile.canRead()) {
            return
        }
        if (cacheFile.path.endsWith(".download")) {
            return
        }
        message?.let {
            updateDb(it, cacheFile, url)
        }
    }

    private fun updateDb(message: Message, cacheFile: File, url: String) {
        Flowable.just(message).compose(RxTransform.flowableToIo())
            .subscribe(object : BaseSubscriber<Message>() {
                override fun onNext(t: Message?) {
                    t?.let {
                        if (it.content != null) {
                            val body = Gson().fromJson(it.content, IMVideoMsgBody::class.java)
                            if (body?.url != null) {
                                val realUrl = getRealUrl(body.url!!)
                                if (realUrl != url) {
                                    return
                                }
                                body.name?.let { name ->
                                    val path = IMCoreManager.storageModule.allocSessionFilePath(
                                        it.sid,
                                        name,
                                        IMFileFormat.Video.value
                                    )
                                    IMCoreManager.storageModule.copyFile(cacheFile.path, path)

                                    if (it.data != null) {
                                        val data =
                                            Gson().fromJson(it.data, IMVideoMsgData::class.java)
                                        data.path = path
                                        it.data = Gson().toJson(data)
                                        IMCoreManager.messageModule
                                            .getMsgProcessor(t.type)
                                            .insertOrUpdateDb(
                                                t,
                                                notify = true,
                                                notifySession = false
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }


}