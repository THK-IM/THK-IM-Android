package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.bean.MessageBean
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.body.ImageBody
import com.thk.im.android.core.processor.body.VideoBody
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.MsgStatus
import com.thk.im.android.db.entity.MsgType
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File
import java.io.FileNotFoundException

open class VideoMsgProcessor : BaseMsgProcessor() {

    private val format = "video"


    override fun messageType(): Int {
        return MsgType.VIDEO.value
    }

    override fun msgBean2Entity(bean: MessageBean): Message {
        val body = Gson().fromJson(bean.body, VideoBody::class.java)
        // 只发送url到对方
        val newBody =
            VideoBody(body.url, null, null, body.duration, body.ratio, body.width, body.height)
        val content = Gson().toJson(newBody)
        return Message(
            bean.clientId, bean.fUId, bean.sessionId, bean.msgId,
            bean.type, content, MsgStatus.SorRSuccess.value, null,
            bean.rMsgId, bean.atUsers, bean.cTime, bean.cTime
        )
    }

    override fun entity2MsgBean(msg: Message): MessageBean {
        val body = Gson().fromJson(msg.content, VideoBody::class.java)
        // 只发送url到对方
        val newBody =
            VideoBody(body.url, null, null, body.duration, body.ratio, body.width, body.height)
        val content = Gson().toJson(newBody)
        return MessageBean(
            msg.id, msg.fUid, msg.sid, msg.msgId, msg.type,
            content, msg.atUsers, msg.rMsgId, msg.cTime
        )
    }

    override fun uploadFlowable(entity: Message): Flowable<Message>? {
        val body = Gson().fromJson(entity.content, VideoBody::class.java)
        val fileName = body.path?.substringAfterLast("/", "")
        if (!body.url.isNullOrEmpty()) {
            return Flowable.just(entity)
        } else if (body.path.isNullOrEmpty() || !File(body.path!!).exists() || fileName.isNullOrBlank()) {
            return Flowable.create({
                it.onError(FileNotFoundException(entity.content))
            }, BackpressureStrategy.LATEST)
        } else {
            val isAssigned = IMManager.getStorageModule()
                .isAssignedPath(body.path!!, fileName, format, entity.sid)
            // 如果不是指定的文件地址,需要先拷贝到im的目录下
            if (!isAssigned) {
                val desPath =
                    IMManager.getStorageModule().allocLocalFilePath(entity.sid, fileName, format)
                val res = IMManager.getStorageModule().copyFile(body.path!!, desPath)
                if (!res) {
                    return Flowable.create({
                        it.onError(FileNotFoundException())
                    }, BackpressureStrategy.LATEST)
                }
                // path 放入本地数据库
                body.path = desPath
                entity.content = Gson().toJson(body)
                updateMsgContent(entity, false)
            }
            val key =
                IMManager.getStorageModule().allocServerFilePath(entity.sid, entity.fUid, fileName)
            return Flowable.create({
                IMManager.getFileLoaderModule()
                    .upload(key, body.path!!, object : LoadListener {
                        override fun onProgress(
                            progress: Int,
                            state: Int,
                            url: String,
                            path: String
                        ) {
                            when (state) {
                                LoadListener.Success -> {
                                    // url 放入本地数据库
                                    body.url = url
                                    entity.content = Gson().toJson(body)
                                    updateMsgContent(entity, true)
                                    it.onNext(entity)
                                }
                                LoadListener.Failed -> {
                                    it.onError(UploadException())
                                }
                                else -> {
                                    // 不用更新数据库，只用发送事件更新ui
                                    entity.extData = Gson().toJson(ImageBody.ExtData(state, progress))
                                    XEventBus.post(XEventType.MsgUpdate.value, entity)
                                }
                            }
                        }

                        override fun notifyOnUiThread(): Boolean {
                            return false
                        }

                    })
            }, BackpressureStrategy.LATEST)

        }
    }

    override fun getSessionDesc(msg: Message): String {
        return "[视频]"
    }
}