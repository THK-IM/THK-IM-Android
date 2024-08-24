package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMLoadType
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.CompressUtils
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadState
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMVideoMsgBody
import com.thk.im.android.ui.manager.IMVideoMsgData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException
import java.io.IOException

open class IMVideoMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.Video.value
    }

    override fun atMeDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.someone_at_me)
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_video_msg)
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
                CompressUtils.compress(
                    videoData.thumbnailPath!!, 100 * 1024, videoData.thumbnailPath!!
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
        storageModule: StorageModule, videoData: IMVideoMsgData, entity: Message
    ): Pair<IMVideoMsgData, Message> {
        val isAssignedPath = storageModule.isAssignedPath(
            videoData.path!!, IMFileFormat.Video.value, entity.sid
        )
        val pair = storageModule.getPathsFromFullPath(videoData.path!!)
        if (!isAssignedPath) {
            val dePath = storageModule.allocSessionFilePath(
                entity.sid, pair.second, IMFileFormat.Video.value
            )
            storageModule.copyFile(videoData.path!!, dePath)
            videoData.path = dePath
            entity.data = Gson().toJson(videoData)
        }
        return Pair(videoData, entity)
    }

    private fun extractVideoFrame(
        storageModule: StorageModule, videoData: IMVideoMsgData, entity: Message
    ): Flowable<Message> {
        try {
            val videoParams =
                CompressUtils.getVideoParams(videoData.path!!) ?: return Flowable.error(
                    IOException("extractVideoFrame error: ${videoData.path!!}")
                )
            val paths = storageModule.getPathsFromFullPath(videoData.path!!)
            val names = storageModule.getFileExt(paths.second)
            val ext = if (videoParams.first.hasAlpha()) {
                "png"
            } else {
                "jpeg"
            }
            val thumbName = "${System.currentTimeMillis() / 1000}_${names.first}_cover.${ext}"
            val thumbnailPath = IMCoreManager.storageModule.allocSessionFilePath(
                entity.sid, thumbName, IMFileFormat.Image.value
            )
            CompressUtils.compressSync(videoParams.first, 4 * 1024 * 1024, thumbnailPath)
            videoParams.first.recycle()
            val sizePair = CompressUtils.getBitmapAspect(thumbnailPath)
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
                    IMCoreManager.fileLoadModule.upload(
                        videoData.thumbnailPath!!,
                        entity,
                        object : LoadListener {

                            override fun onProgress(
                                progress: Int,
                                state: Int,
                                url: String,
                                path: String,
                                exception: Exception?
                            ) {
                                XEventBus.post(
                                    IMEvent.MsgLoadStatusUpdate.value, IMLoadProgress(
                                        IMLoadType.Upload.value, url, path, state, progress
                                    )
                                )
                                when (state) {
                                    FileLoadState.Init.value, FileLoadState.Wait.value, FileLoadState.Ing.value -> {
                                    }

                                    FileLoadState.Success.value -> {
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
                                    }

                                    else -> {
                                        if (exception != null) {
                                            it.onError(exception)
                                        } else {
                                            it.onError(RuntimeException())
                                        }
                                    }
                                }
                            }

                            override fun notifyOnUiThread(): Boolean {
                                return false
                            }
                        })
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
                return Flowable.create({
                    IMCoreManager.fileLoadModule.upload(videoData.path!!,
                        entity,
                        object : LoadListener {

                            override fun onProgress(
                                progress: Int,
                                state: Int,
                                url: String,
                                path: String,
                                exception: Exception?
                            ) {
                                XEventBus.post(
                                    IMEvent.MsgLoadStatusUpdate.value, IMLoadProgress(
                                        IMLoadType.Upload.value, url, path, state, progress
                                    )
                                )
                                when (state) {
                                    FileLoadState.Init.value, FileLoadState.Wait.value, FileLoadState.Ing.value -> {
                                    }

                                    FileLoadState.Success.value -> {
                                        if (videoBody == null) {
                                            videoBody = IMVideoMsgBody()
                                        }
                                        videoBody.name = pair.second
                                        videoBody.url = url
                                        entity.content = Gson().toJson(videoBody)
                                        it.onNext(entity)
                                        it.onComplete()
                                    }

                                    else -> {
                                        if (exception != null) {
                                            it.onError(exception)
                                        } else {
                                            it.onError(RuntimeException())
                                        }
                                    }
                                }
                            }

                            override fun notifyOnUiThread(): Boolean {
                                return false
                            }
                        })
                }, BackpressureStrategy.LATEST)
            }
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return Flowable.error(e)
        }
    }

    override fun downloadMsgContent(entity: Message, resourceType: String): Boolean {
        if (entity.content.isNullOrEmpty()) {
            return false
        }
        var data = Gson().fromJson(entity.data, IMVideoMsgData::class.java)
        val body = Gson().fromJson(entity.content, IMVideoMsgBody::class.java)

        val downloadUrl = if (resourceType == IMMsgResourceType.Thumbnail.value) {
            body.thumbnailUrl
        } else {
            body.url
        }

        var fileName = body.name
        if (downloadUrl == null || fileName == null) {
            return true
        }

        if (downLoadingUrls.contains(downloadUrl)) {
            return true
        } else {
            downLoadingUrls.add(downloadUrl)
        }

        if (resourceType == IMMsgResourceType.Thumbnail.value) {
            fileName = "cover_${fileName}.jpeg"
        }

        val listener = object : LoadListener {
            override fun onProgress(
                progress: Int, state: Int, url: String, path: String, exception: Exception?
            ) {
                XEventBus.post(
                    IMEvent.MsgLoadStatusUpdate.value,
                    IMLoadProgress(IMLoadType.Download.value, url, path, state, progress)
                )
                when (state) {
                    FileLoadState.Init.value, FileLoadState.Wait.value, FileLoadState.Ing.value -> {
                    }

                    FileLoadState.Success.value -> {
                        if (data == null) {
                            data = IMVideoMsgData()
                        }
                        val localPath = IMCoreManager.storageModule.allocSessionFilePath(
                            entity.sid, fileName, IMFileFormat.Image.value
                        )
                        IMCoreManager.storageModule.copyFile(path, localPath)
                        data.height = body.height
                        data.width = body.width
                        if (resourceType == IMMsgResourceType.Thumbnail.value) {
                            data.thumbnailPath = localPath
                        } else {
                            data.path = localPath
                        }
                        entity.data = Gson().toJson(data)
                        insertOrUpdateDb(entity, notify = true, notifySession = false)
                        downLoadingUrls.remove(downloadUrl)
                    }

                    else -> {
                        downLoadingUrls.remove(downloadUrl)
                    }
                }
            }

            override fun notifyOnUiThread(): Boolean {
                return false
            }

        }
        IMCoreManager.fileLoadModule.download(downloadUrl, entity, listener)
        return true
    }
}