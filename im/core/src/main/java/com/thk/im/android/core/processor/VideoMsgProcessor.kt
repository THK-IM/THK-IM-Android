package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMLoadType
import com.thk.im.android.core.IMVideoMsgBody
import com.thk.im.android.core.IMVideoMsgData
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException
import java.io.IOException

open class VideoMsgProcessor : BaseMsgProcessor() {

    private val format = "video"


    override fun messageType(): Int {
        return MsgType.VIDEO.value
    }

//    override fun uploadFlowable(entity: Message): Flowable<Message>? {
//        val body = Gson().fromJson(entity.content, VideoBody::class.java)
//        val fileName = body.path?.substringAfterLast("/", "")
//        if (!body.url.isNullOrEmpty()) {
//            return Flowable.just(entity)
//        } else if (body.path.isNullOrEmpty() || !File(body.path!!).exists() || fileName.isNullOrBlank()) {
//            return Flowable.create({
//                it.onError(FileNotFoundException(entity.content))
//            }, BackpressureStrategy.LATEST)
//        } else {
//            val isAssigned = IMCoreManager.getStorageModule()
//                .isAssignedPath(body.path!!, fileName, format, entity.sid)
//            // 如果不是指定的文件地址,需要先拷贝到im的目录下
//            if (!isAssigned) {
//                val desPath =
//                    IMCoreManager.getStorageModule()
//                        .allocSessionFilePath(entity.sid, fileName, format)
//                val res = IMCoreManager.getStorageModule().copyFile(body.path!!, desPath)
//                if (!res) {
//                    return Flowable.create({
//                        it.onError(FileNotFoundException())
//                    }, BackpressureStrategy.LATEST)
//                }
//                // path 放入本地数据库
//                body.path = desPath
//                entity.content = Gson().toJson(body)
//                insertOrUpdateDb(entity)
//            }
//            val key =
//                IMCoreManager.fileLoaderModule
//                    .getUploadKey(entity.sid, entity.fUid, fileName, entity.id)
//            return Flowable.create({
//                IMCoreManager.fileLoaderModule
//                    .upload(key, body.path!!, object : LoadListener {
//                        override fun onProgress(
//                            progress: Int,
//                            state: Int,
//                            url: String,
//                            path: String
//                        ) {
//                            when (state) {
//                                LoadListener.Success -> {
//                                    // url 放入本地数据库
//                                    body.url = url
//                                    entity.content = Gson().toJson(body)
//                                    insertOrUpdateDb(entity)
//                                    it.onNext(entity)
//                                }
//
//                                LoadListener.Failed -> {
//                                    it.onError(UploadException())
//                                }
//
//                                else -> {
//                                    // 不用更新数据库，只用发送事件更新ui
////                                    entity.data =
////                                        Gson().toJson(ImageBody.ExtData(state, progress))
//                                    XEventBus.post(IMEvent.MsgUpdate.value, entity)
//                                }
//                            }
//                        }
//
//                        override fun notifyOnUiThread(): Boolean {
//                            return false
//                        }
//
//                    })
//            }, BackpressureStrategy.LATEST)
//
//        }
//    }

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
                Flowable.just(pair.second)
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
            val thumbName = "${names.first}_cover.jpg"
            val thumbnailPath = IMCoreManager.storageModule
                .allocSessionFilePath(entity.sid, thumbName, IMFileFormat.Image.value)
            storageModule.saveImageInto(thumbnailPath, videoParams.first)
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
                val pair = IMCoreManager.storageModule.getPathsFromFullPath(videoData.thumbnailPath!!)
                return Flowable.create({
                    val key = IMCoreManager.fileLoaderModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoaderModule
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
                                        videoBody.thumbnailUrl = url
                                        videoBody.duration = videoData.duration
                                        videoBody.width = videoData.width
                                        videoBody.height = videoData.height
                                        entity.content = Gson().toJson(videoBody)
                                        insertOrUpdateDb(entity, false)
                                        it.onNext(entity)
                                        it.onComplete()
                                    }

                                    else -> {
                                        it.onError(UploadException())
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
                    val key = IMCoreManager.fileLoaderModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoaderModule
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
                                        videoBody.url = url
                                        entity.content = Gson().toJson(videoBody)
                                        it.onNext(entity)
                                        it.onComplete()
                                    }

                                    else -> {
                                        it.onError(UploadException())
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
}