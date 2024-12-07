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
import com.thk.im.android.ui.manager.IMImageMsgBody
import com.thk.im.android.ui.manager.IMImageMsgData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

open class IMImageMsgProcessor : IMBaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.Image.value
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_image_msg)
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
        storageModule: StorageModule, imageData: IMImageMsgData, entity: Message
    ): Pair<IMImageMsgData, Message> {
        val isAssignedPath = storageModule.isAssignedPath(
            imageData.path!!, IMFileFormat.Image.value, entity.sid
        )
        val pair = storageModule.getPathsFromFullPath(imageData.path!!)
        if (!isAssignedPath) {
            val dePath = storageModule.allocSessionFilePath(
                entity.sid, pair.second, IMFileFormat.Image.value
            )
            storageModule.copyFile(imageData.path!!, dePath)
            imageData.path = dePath
            entity.data = Gson().toJson(imageData)
        }
        return Pair(imageData, entity)
    }

    private fun compress(
        storageModule: StorageModule, imageData: IMImageMsgData, entity: Message
    ): Flowable<Message> {
        val paths = storageModule.getPathsFromFullPath(imageData.path!!)
        val names = storageModule.getFileExt(paths.second)
        val thumbName = "${names.first}_thumb.${names.second}"
        val thumbPath =
            storageModule.allocSessionFilePath(entity.sid, thumbName, IMFileFormat.Image.value)
        return CompressUtils.compress(
            imageData.path!!, 100 * 1024, thumbPath
        ).flatMap {
            val size = CompressUtils.getBitmapAspect(imageData.path!!)
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
            LLog.v("uploadThumbImage start")
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
                    IMCoreManager.fileLoadModule.upload(
                        imageData.thumbnailPath!!,
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
                                        if (imageBody == null) {
                                            imageBody = IMImageMsgBody()
                                        }
                                        imageBody.thumbnailUrl = url
                                        imageBody.name = pair.second
                                        imageBody.width = imageData.width
                                        imageBody.height = imageData.height
                                        entity.content = Gson().toJson(imageBody)
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

    private fun uploadOriginImage(entity: Message): Flowable<Message> {
        LLog.v("uploadOriginImage start")
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
                    IMCoreManager.fileLoadModule.upload(
                        imageData.path!!,
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
                                        if (imageBody == null) {
                                            imageBody = IMImageMsgBody()
                                        }
                                        imageBody.url = url
                                        imageBody.name = pair.second
                                        entity.content = Gson().toJson(imageBody)
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
        var data = Gson().fromJson(entity.data, IMImageMsgData::class.java)
        val body = Gson().fromJson(entity.content, IMImageMsgBody::class.java)

        val downloadUrl = if (resourceType == IMMsgResourceType.Thumbnail.value) {
            body.thumbnailUrl
        } else {
            body.url
        }

        var fileName = body.name
        if (downloadUrl == null || fileName == null) {
            return false
        }

        if (downLoadingUrls.contains(downloadUrl)) {
            return true
        } else {
            downLoadingUrls.add(downloadUrl)
        }

        if (resourceType == IMMsgResourceType.Thumbnail.value) {
            fileName = "thumb_${fileName}"
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
                            data = IMImageMsgData()
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