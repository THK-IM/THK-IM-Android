package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.body.VoiceBody
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File
import java.io.FileNotFoundException

class VoiceMsgProcessor : BaseMsgProcessor() {

    private val format = "voice"

    override fun messageType(): Int {
        return MsgType.VOICE.value
    }

    override fun uploadFlowable(entity: Message): Flowable<Message> {
        val voiceBody = Gson().fromJson(entity.content, VoiceBody::class.java)
        val fileName = voiceBody.path?.substringAfterLast("/", "")
        if (!voiceBody.url.isNullOrEmpty()) {
            return Flowable.just(entity)
        } else if (voiceBody.path.isNullOrEmpty() || !File(voiceBody.path!!).exists() || fileName.isNullOrBlank()) {
            return Flowable.create({
                it.onError(FileNotFoundException(entity.content))
            }, BackpressureStrategy.LATEST)
        } else {
            return Flowable.create({
                val key = IMCoreManager.getStorageModule().allocSessionFilePath(
                    entity.sid, entity.fUid,
                    fileName,
                    format
                ).second
                IMCoreManager.getFileLoaderModule()
                    .upload(key, voiceBody.path!!, object : LoadListener {

                        override fun onProgress(
                            progress: Int,
                            state: Int,
                            url: String,
                            path: String
                        ) {
                            entity.extData = Gson().toJson(VoiceBody.ExtData(state, progress))
                            when (state) {
                                LoadListener.Success -> {
                                    voiceBody.url = url
                                    voiceBody.path = null
                                    entity.content = Gson().toJson(voiceBody)
                                    it.onNext(entity)
                                }

                                LoadListener.Failed -> {
                                    it.onError(UploadException())
                                }

                                else -> {
                                    // 不用更新数据库，只用发送事件更新ui
                                    XEventBus.post(XEventType.MsgUpdate.value, entity)
                                }
                            }
                        }

                        override fun notifyOnUiThread(): Boolean {
                            return false
                        }
                    })
            }, BackpressureStrategy.BUFFER)
        }
    }


    override fun getSessionDesc(msg: Message): String {
        return "[语音消息]"
    }
}