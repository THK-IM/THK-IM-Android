package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.base.LLog
import com.thk.im.android.core.IMAudioMsgBody
import com.thk.im.android.core.IMAudioMsgData
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.core.IMImageMsgBody
import com.thk.im.android.core.IMUploadProgress
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.FileNotFoundException

class AudioMsgProcessor : BaseMsgProcessor() {

    private val format = "voice"

    override fun messageType(): Int {
        return MsgType.Audio.value
    }

    override fun getSessionDesc(msg: Message): String {
        return "[语音消息]"
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message> {
        try {
            val storageModule = IMCoreManager.getStorageModule()
            val audioData = Gson().fromJson(message.data, IMAudioMsgData::class.java)
            if (audioData.path == null || audioData.duration == null) {
                return Flowable.error(FileNotFoundException())
            }
            val pair = checkDir(storageModule, audioData, message)
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
                val pair = IMCoreManager.getStorageModule().getPathsFromFullPath(audioData.path!!)
                return Flowable.create({
                    val key = IMCoreManager.fileLoaderModule.getUploadKey(
                        entity.sid,
                        entity.fUid,
                        pair.second,
                        entity.id
                    )
                    IMCoreManager.fileLoaderModule
                        .upload(key, audioData.path!!, object : LoadListener {

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
                                        if (audioBody == null) {
                                            audioBody = IMAudioMsgBody()
                                        }
                                        audioBody.url = url
                                        audioBody.duration = audioData.duration!!
                                        entity.content = Gson().toJson(audioBody)
                                        entity.sendStatus = MsgSendStatus.Sending.value
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