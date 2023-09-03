package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMImageMsgBody
import com.thk.im.android.core.IMImageMsgData
import com.thk.im.android.core.IMUploadProgress
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

class ImageMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.IMAGE.value
    }

    override fun getSessionDesc(msg: Message): String {
        return "[图片]"
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message> {
        try {
            val storageModule = IMCoreManager.getStorageModule()
            var imageData = Gson().fromJson(message.data, IMImageMsgData::class.java)
            if (imageData.path == null) {
                return Flowable.error(FileNotFoundException())
            }
            val pair = checkDir(storageModule, imageData, message)
            imageData = pair.first
            return if (imageData.thumbnailPath == null) {
                compress(storageModule, imageData, pair.second)
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
        return MediaUtils.compress(IMCoreManager.getApplication(), imageData.path!!, 100).flatMap {
            val paths = storageModule.getPathsFromFullPath(imageData.path!!)
            val names = storageModule.getFileExt(paths.second)
            val thumbName = "${names.first}_thumb.${names.second}"
            val thumbPath =
                storageModule.allocSessionFilePath(entity.sid, thumbName, IMFileFormat.Image.value)
            val success = storageModule.copyFile(it, thumbPath)
            if (!success) {
                return@flatMap Flowable.error(IOException("copy $it to $thumbPath error"))
            }
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
            val imageBody = Gson().fromJson(entity.content, IMImageMsgBody::class.java)
            if (!imageBody.thumbnailUrl.isNullOrEmpty()) {
                return Flowable.just(entity)
            } else if (imageData.thumbnailPath.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair =
                    IMCoreManager.getStorageModule().getPathsFromFullPath(imageData.thumbnailPath!!)
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
                                when (state) {
                                    LoadListener.Init,
                                    LoadListener.Wait,
                                    LoadListener.Ing -> {
                                        XEventBus.post(
                                            IMEvent.MsgUploadProgressUpdate.value,
                                            IMUploadProgress(key, state, progress)
                                        )
                                    }

                                    LoadListener.Success -> {
                                        imageBody.thumbnailUrl = url
                                        imageBody.width = imageData.width
                                        imageBody.height = imageData.height
                                        entity.content = Gson().toJson(imageBody)
                                        insertOrUpdateDb(entity, false)
                                        it.onNext(entity)
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
            val imageBody = Gson().fromJson(entity.content, IMImageMsgBody::class.java)
            if (!imageBody.url.isNullOrEmpty()) {
                return Flowable.just(entity)
            } else if (imageData.path.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair = IMCoreManager.getStorageModule().getPathsFromFullPath(imageData.path!!)
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
                                when (state) {
                                    LoadListener.Init,
                                    LoadListener.Wait,
                                    LoadListener.Ing -> {
                                        XEventBus.post(
                                            IMEvent.MsgUploadProgressUpdate.value,
                                            IMUploadProgress(key, state, progress)
                                        )
                                    }

                                    LoadListener.Success -> {
                                        imageBody.url = url
                                        entity.content = Gson().toJson(imageBody)
                                        it.onNext(entity)
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