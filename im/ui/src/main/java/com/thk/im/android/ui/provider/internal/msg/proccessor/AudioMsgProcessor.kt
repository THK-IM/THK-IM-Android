package com.thk.im.android.ui.provider.internal.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.base.LLog
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMLoadProgress
import com.thk.im.android.core.IMLoadType
import com.thk.im.android.core.IMMsgResourceType
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.manager.IMAudioMsgBody
import com.thk.im.android.ui.manager.IMAudioMsgData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

class AudioMsgProcessor : BaseMsgProcessor() {

    override fun messageType(): Int {
        return MsgType.Audio.value
    }

    override fun getSessionDesc(msg: Message): String {
        return "[语音消息]"
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message> {
        try {
            val audioData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            if (audioData.path == null || audioData.duration == null) {
                return Flowable.error(FileNotFoundException())
            }
            val pair = checkDir(IMCoreManager.storageModule, audioData, message)
            return Flowable.just(pair.second)
        } catch (e: Exception) {
            e.message?.let { LLog.e(it) }
            return Flowable.error(e)
        }
    }

    @Throws(Exception::class)
    private fun checkDir(
        storageModule: StorageModule,
        audioData: IMAudioMsgData,
        entity: Message
    ): Pair<IMAudioMsgData, Message> {
        val isAssignedPath = storageModule.isAssignedPath(
            audioData.path!!,
            IMFileFormat.Image.value,
            entity.sid
        )
        val pair = storageModule.getPathsFromFullPath(audioData.path!!)
        if (!isAssignedPath) {
            val dePath = storageModule.allocSessionFilePath(
                entity.sid,
                pair.second,
                IMFileFormat.Audio.value
            )
            storageModule.copyFile(audioData.path!!, dePath)
            audioData.path = dePath
            entity.data = Gson().toJson(audioData)
        }
        return Pair(audioData, entity)
    }

    override fun uploadFlowable(entity: Message): Flowable<Message> {
        return this.uploadAudio(entity)
    }

    private fun uploadAudio(entity: Message): Flowable<Message> {
        try {
            val audioData = Gson().fromJson(entity.data, IMAudioMsgData::class.java)
            var audioBody = Gson().fromJson(entity.content, IMAudioMsgBody::class.java)
            if (audioBody != null) {
                if (!audioBody.url.isNullOrEmpty()) {
                    return Flowable.just(entity)
                }
            }
            if (audioData == null || audioData.path.isNullOrEmpty()) {
                return Flowable.error(FileNotFoundException())
            } else {
                val pair = IMCoreManager.storageModule.getPathsFromFullPath(audioData.path!!)
                return Flowable.create({

                    val key = IMCoreManager.fileLoadModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    var over = false
                    IMCoreManager.fileLoadModule
                        .upload(key, audioData.path!!, object : LoadListener {

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
                                        if (audioBody == null) {
                                            audioBody = IMAudioMsgBody()
                                        }
                                        audioBody.url = url
                                        audioBody.name = pair.second
                                        audioBody.duration = audioData.duration!!
                                        entity.content = Gson().toJson(audioBody)
                                        entity.sendStatus = MsgSendStatus.Sending.value
                                        it.onNext(entity)
                                        it.onComplete()
                                        over = true
                                    }

                                    else -> {
                                        over = true
                                        it.onError(UploadException())
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

    override fun downloadMsgContent(entity: Message, resourceType: String): Boolean {

        var data = Gson().fromJson(entity.data, IMAudioMsgData::class.java)
        val body = Gson().fromJson(entity.content, IMAudioMsgBody::class.java)

        val downloadUrl = if (resourceType == IMMsgResourceType.Thumbnail.value) {
            body.url
        } else {
            body.url
        }

        val fileName = body.name
        if (downloadUrl == null || fileName == null) {
            return false
        }

        val localPath = IMCoreManager.storageModule.allocSessionFilePath(
            entity.sid,
            fileName,
            IMFileFormat.Image.value
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
                        if (data == null) {
                            data = IMAudioMsgData()
                        }
                        data.duration = body.duration
                        if (resourceType == IMMsgResourceType.Thumbnail.value) {
                            data.path = path
                        } else {
                            data.path = path
                        }
                        entity.data = Gson().toJson(data)
                        insertOrUpdateDb(entity, notify = true, notifySession = false)
                    }

                    else -> {
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
        return true
    }
}