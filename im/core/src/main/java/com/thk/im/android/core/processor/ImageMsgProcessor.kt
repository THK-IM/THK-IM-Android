package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMImageMsgBody
import com.thk.im.android.core.IMImageMsgData
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMLoadType
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.DownloadException
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

class ImageMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.IMAGE.value
    }

    override fun getSessionDesc(msg: Message): String {
        return "[图片]"
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message> {
        try {
            var imageData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            if (imageData.path == null) {
                return Flowable.error(FileNotFoundException())
            }
            val pair = checkDir(IMCoreManager.storageModule, imageData, message)
            imageData = pair.first
            return if (imageData.thumbnailPath == null) {
                compress(IMCoreManager.storageModule, imageData, pair.second)
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
        imageData: IMImageMsgData,
        entity: Message
    ): Pair<IMImageMsgData, Message> {
        val isAssignedPath = storageModule.isAssignedPath(
            imageData.path!!,
            IMFileFormat.Image.value,
            entity.sid
        )
        val pair = storageModule.getPathsFromFullPath(imageData.path!!)
        if (!isAssignedPath) {
            val dePath = storageModule.allocSessionFilePath(
                entity.sid,
                pair.second,
                IMFileFormat.Image.value
            )
            storageModule.copyFile(imageData.path!!, dePath)
            imageData.path = dePath
            entity.data = Gson().toJson(imageData)
        }
        return Pair(imageData, entity)
    }

    private fun compress(
        storageModule: StorageModule,
        imageData: IMImageMsgData,
        entity: Message
    ): Flowable<Message> {
        val paths = storageModule.getPathsFromFullPath(imageData.path!!)
        val names = storageModule.getFileExt(paths.second)
        val thumbName = "${names.first}_thumb.${names.second}"
        val thumbPath =
            storageModule.allocSessionFilePath(entity.sid, thumbName, IMFileFormat.Image.value)
        return MediaUtils.compress(
            imageData.path!!,
            100 * 1024,
            thumbPath
        ).flatMap {
            val size = MediaUtils.getBitmapAspect(imageData.path!!)
            imageData.thumbnailPath = thumbPath
            imageData.width = size.first
            imageData.height = size.second
            entity.data = Gson().toJson(imageData)
            return@flatMap Flowable.just(entity)
        }
    }

    override fun uploadFlowable(entity: Message): Flowable<Message>? {
        return this.uploadThumbImage(entity).flatMap {
            return@flatMap this.uploadOriginImage(it)
        }
    }

    private fun uploadThumbImage(entity: Message): Flowable<Message> {
        try {
            val imageData = Gson().fromJson(entity.data, IMImageMsgData::class.java)
            var imageBody = Gson().fromJson(entity.content, IMImageMsgBody::class.java)
            if (imageBody != null) {
                if (!imageBody.thumbnailUrl.isNullOrEmpty()) {
                    return Flowable.just(entity)
                }
            }
            if (imageData == null || imageData.thumbnailPath.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair =
                    IMCoreManager.storageModule.getPathsFromFullPath(imageData.thumbnailPath!!)
                return Flowable.create({
                    val key = IMCoreManager.fileLoaderModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoaderModule
                        .upload(key, imageData.thumbnailPath!!, object : LoadListener {

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
                                        if (imageBody == null) {
                                            imageBody = IMImageMsgBody()
                                        }
                                        imageBody.thumbnailUrl = url
                                        imageBody.width = imageData.width
                                        imageBody.height = imageData.height
                                        entity.content = Gson().toJson(imageBody)
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

    private fun uploadOriginImage(entity: Message): Flowable<Message> {
        try {
            val imageData = Gson().fromJson(entity.data, IMImageMsgData::class.java)
            var imageBody = Gson().fromJson(entity.content, IMImageMsgBody::class.java)
            if (imageBody != null) {
                if (!imageBody.url.isNullOrEmpty()) {
                    return Flowable.just(entity)
                }
            }
            if (imageData == null || imageData.path.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                if (imageData.path.equals(imageData.thumbnailPath)) {
                    imageBody.url = imageBody.thumbnailUrl
                    entity.content = Gson().toJson(imageBody)
                    return Flowable.just(entity)
                }
                val pair = IMCoreManager.storageModule.getPathsFromFullPath(imageData.path!!)
                return Flowable.create({
                    val key = IMCoreManager.fileLoaderModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoaderModule
                        .upload(key, imageData.path!!, object : LoadListener {

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
                                        if (imageBody == null) {
                                            imageBody = IMImageMsgBody()
                                        }
                                        imageBody.url = url
                                        entity.content = Gson().toJson(imageBody)
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

    override fun downloadMsgContent(entity: Message, resourceType: String) {
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message) {
                super.onComplete()
                insertOrUpdateDb(t, notify = true, notifySession = false)
            }
        }
        Flowable.create(
            {
                var imageData = Gson().fromJson(entity.data, IMImageMsgData::class.java)
                val imageBody = Gson().fromJson(entity.content, IMImageMsgBody::class.java)

                val downloadUrl = if (resourceType == IMMsgResourceType.Thumbnail.value) {
                    imageBody.thumbnailUrl!!
                } else {
                    imageBody.url!!
                }

                val fileName = downloadUrl.substringAfterLast("/", "")
                val localPath = IMCoreManager.storageModule.allocSessionFilePath(
                    entity.sid,
                    fileName,
                    "img"
                )

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
                                if (imageData == null) {
                                    imageData = IMImageMsgData()
                                }
                                imageData.height = imageBody.height
                                imageData.width = imageBody.width
                                if (resourceType == IMMsgResourceType.Thumbnail.value) {
                                    imageData.thumbnailPath = path
                                } else {
                                    imageData.path = path
                                }
                                entity.data = Gson().toJson(imageData)
                                it.onNext(entity)
                                it.onComplete()
                            }

                            else -> {
                                it.onError(DownloadException())
                            }
                        }
                    }

                    override fun notifyOnUiThread(): Boolean {
                        return false
                    }

                }
                IMCoreManager.fileLoaderModule.download(
                    downloadUrl,
                    localPath,
                    listener
                )

            }, BackpressureStrategy.LATEST
        ).compose(RxTransform.flowableToIo()).subscribe(subscriber)
    }

}