package com.thk.im.android.ui.provider.internal.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMLoadType
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.DownloadException
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException
import java.io.IOException

open class VideoMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.VIDEO.value
    }

    override fun getSessionDesc(msg: Message): String {
        return "[视频]"
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message> {
        try {
            var videoData = Gson().fromJson(message.data, IMVideoMsgData::class.java)
            if (videoData.path == null) {
                return Flowable.error(FileNotFoundException())
            }
            val pair = checkDir(IMCoreManager.storageModule, videoData, message)
            videoData = pair.first
            return if (videoData.thumbnailPath == null) {
                extractVideoFrame(IMCoreManager.storageModule, videoData, pair.second)
            } else {
                MediaUtils.compress(
                    videoData.thumbnailPath!!,
                    100 * 1024,
                    videoData.thumbnailPath!!
                ).flatMap {
                    Flowable.just(message)
                }
            }
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return Flowable.error(e)
        }
    }

    @Throws(Exception::class)
    private fun checkDir(
        storageModule: StorageModule,
        videoData: IMVideoMsgData,
        entity: Message
    ): Pair<IMVideoMsgData, Message> {
        val isAssignedPath = storageModule.isAssignedPath(
            videoData.path!!,
            IMFileFormat.Video.value,
            entity.sid
        )
        val pair = storageModule.getPathsFromFullPath(videoData.path!!)
        if (!isAssignedPath) {
            val dePath = storageModule.allocSessionFilePath(
                entity.sid,
                pair.second,
                IMFileFormat.Video.value
            )
            storageModule.copyFile(videoData.path!!, dePath)
            videoData.path = dePath
            entity.data = Gson().toJson(videoData)
        }
        return Pair(videoData, entity)
    }

    private fun extractVideoFrame(
        storageModule: StorageModule,
        videoData: IMVideoMsgData,
        entity: Message
    ): Flowable<Message> {
        try {
            val videoParams = MediaUtils.getVideoParams(videoData.path!!)
                ?: return Flowable.error(IOException("extractVideoFrame error: ${videoData.path!!}"))
            val paths = storageModule.getPathsFromFullPath(videoData.path!!)
            val names = storageModule.getFileExt(paths.second)
            val ext = if (videoParams.first.hasAlpha()) {
                "png"
            } else {
                "jpeg"
            }
            val thumbName = "${System.currentTimeMillis()/1000}_${names.first}_cover.${ext}"
            val thumbnailPath = IMCoreManager.storageModule
                .allocSessionFilePath(entity.sid, thumbName, IMFileFormat.Image.value)
            MediaUtils.compressSync(videoParams.first, 100 * 1024, thumbnailPath)
            videoParams.first.recycle()
            val sizePair = MediaUtils.getBitmapAspect(thumbnailPath)
            videoData.thumbnailPath = thumbnailPath
            videoData.duration = videoParams.second
            videoData.width = sizePair.first
            videoData.height = sizePair.second
            entity.data = Gson().toJson(videoData)
            return Flowable.just(entity)
        } catch (e: Exception) {
            LLog.e(e.toString())
            return Flowable.error(e)
        }
    }

    override fun uploadFlowable(entity: Message): Flowable<Message>? {
        return this.uploadCoverImage(entity).flatMap {
            return@flatMap this.uploadVideo(it)
        }
    }

    private fun uploadCoverImage(entity: Message): Flowable<Message> {
        try {
            val videoData = Gson().fromJson(entity.data, IMVideoMsgData::class.java)
            var videoBody = Gson().fromJson(entity.content, IMVideoMsgBody::class.java)
            if (videoBody != null) {
                if (!videoBody.thumbnailUrl.isNullOrEmpty()) {
                    return Flowable.just(entity)
                }
            }
            if (videoData == null || videoData.thumbnailPath.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair =
                    IMCoreManager.storageModule.getPathsFromFullPath(videoData.thumbnailPath!!)
                return Flowable.create({
                    val key = IMCoreManager.fileLoadModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    var over = false
                    IMCoreManager.fileLoadModule
                        .upload(key, videoData.thumbnailPath!!, object : LoadListener {

                            override fun onProgress(
                                progress: Int,
                                state: Int,
                                url: String,
                                path: String
                            ) {
                                XEventBus.post(
                                    IMEvent.MsgLoadStatusUpdate.value,
                                    IMLoadProgress(IMLoadType.Upload.value, key, state, progress)
                                )
                                when (state) {
                                    LoadListener.Init,
                                    LoadListener.Wait,
                                    LoadListener.Ing -> {
                                    }

                                    LoadListener.Success -> {
                                        if (videoBody == null) {
                                            videoBody = IMVideoMsgBody()
                                        }
                                        videoBody.name = pair.second
                                        videoBody.thumbnailUrl = url
                                        videoBody.duration = videoData.duration
                                        videoBody.width = videoData.width
                                        videoBody.height = videoData.height
                                        entity.content = Gson().toJson(videoBody)
                                        insertOrUpdateDb(
                                            entity,
                                            notify = false,
                                            notifySession = false,
                                        )
                                        it.onNext(entity)
                                        it.onComplete()
                                        over = true
                                    }

                                    else -> {
                                        it.onError(UploadException())
                                        over = true
                                    }
                                }
                            }

                            override fun notifyOnUiThread(): Boolean {
                                return false
                            }
                        })
                    // 防止线程被回收
                    while (!over) {
                        Thread.sleep(200)
                    }
                }, BackpressureStrategy.LATEST)
            }
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return Flowable.error(e)
        }
    }

    private fun uploadVideo(entity: Message): Flowable<Message> {
        try {
            val videoData = Gson().fromJson(entity.data, IMVideoMsgData::class.java)
            var videoBody = Gson().fromJson(entity.content, IMVideoMsgBody::class.java)
            if (videoBody != null) {
                if (!videoBody.url.isNullOrEmpty()) {
                    return Flowable.just(entity)
                }
            }
            if (videoData == null || videoData.path.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair = IMCoreManager.storageModule.getPathsFromFullPath(videoData.path!!)
                var over = false
                return Flowable.create({
                    val key = IMCoreManager.fileLoadModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoadModule
                        .upload(key, videoData.path!!, object : LoadListener {

                            override fun onProgress(
                                progress: Int,
                                state: Int,
                                url: String,
                                path: String
                            ) {
                                XEventBus.post(
                                    IMEvent.MsgLoadStatusUpdate.value,
                                    IMLoadProgress(IMLoadType.Upload.value, key, state, progress)
                                )
                                when (state) {
                                    LoadListener.Init,
                                    LoadListener.Wait,
                                    LoadListener.Ing -> {
                                    }

                                    LoadListener.Success -> {
                                        if (videoBody == null) {
                                            videoBody = IMVideoMsgBody()
                                        }
                                        videoBody.name = pair.second
                                        videoBody.url = url
                                        entity.content = Gson().toJson(videoBody)
                                        it.onNext(entity)
                                        it.onComplete()
                                        over = true
                                    }

                                    else -> {
                                        it.onError(UploadException())
                                        over = true
                                    }
                                }
                            }

                            override fun notifyOnUiThread(): Boolean {
                                return false
                            }
                        })
                    // 防止线程被回收
                    while (!over) {
                        Thread.sleep(200)
                    }
                }, BackpressureStrategy.LATEST)
            }
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return Flowable.error(e)
        }
    }

    override fun downloadMsgContent(entity: Message, resourceType: String) {
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                super.onComplete()
                insertOrUpdateDb(t, notify = true, notifySession = false)
            }
        }
        Flowable.create(
            {
                var data = Gson().fromJson(entity.data, IMVideoMsgData::class.java)
                val body = Gson().fromJson(entity.content, IMVideoMsgBody::class.java)

                val downloadUrl = if (resourceType == IMMsgResourceType.Thumbnail.value) {
                    body.thumbnailUrl
                } else {
                    body.url
                }

                val fileName = body.name
                if (downloadUrl == null || fileName == null) {
                    it.onError(DownloadException())
                    return@create
                }

                val localPath = IMCoreManager.storageModule.allocSessionFilePath(
                    entity.sid,
                    fileName,
                    IMFileFormat.Image.value
                )
                var over = false
                val listener = object : LoadListener {
                    override fun onProgress(
                        progress: Int,
                        state: Int,
                        url: String,
                        path: String
                    ) {
                        XEventBus.post(
                            IMEvent.MsgLoadStatusUpdate.value,
                            IMLoadProgress(IMLoadType.Download.value, url, state, progress)
                        )
                        when (state) {
                            LoadListener.Init,
                            LoadListener.Wait,
                            LoadListener.Ing -> {
                            }

                            LoadListener.Success -> {
                                if (data == null) {
                                    data = IMVideoMsgData()
                                }
                                data.height = body.height
                                data.width = body.width
                                if (resourceType == IMMsgResourceType.Thumbnail.value) {
                                    data.thumbnailPath = path
                                } else {
                                    data.path = path
                                }
                                entity.data = Gson().toJson(data)
                                it.onNext(entity)
                                it.onComplete()
                                over = true
                            }

                            else -> {
                                it.onError(DownloadException())
                                over = true
                            }
                        }
                    }

                    override fun notifyOnUiThread(): Boolean {
                        return false
                    }

                }
                IMCoreManager.fileLoadModule.download(
                    downloadUrl,
                    localPath,
                    listener
                )

                // 防止线程被回收
                while (!over) {
                    Thread.sleep(200)
                }
            }, BackpressureStrategy.LATEST
        ).compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }
}