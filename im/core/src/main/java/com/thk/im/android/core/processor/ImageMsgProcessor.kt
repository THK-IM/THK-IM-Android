package com.thk.im.android.core.processor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.exception.UploadException
import com.thk.im.android.core.fileloader.LoadListener
import com.thk.im.android.core.processor.body.ImageBody
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File
import java.io.FileNotFoundException

class ImageMsgProcessor : BaseMsgProcessor() {

    private val format = "img"

    override fun messageType(): Int {
        return MsgType.IMAGE.value
    }


    override fun uploadFlowable(entity: Message): Flowable<Message> {
        val body = Gson().fromJson(entity.content, ImageBody::class.java)
        val fileName = body.path?.substringAfterLast("/", "")
        if (!body.url.isNullOrEmpty()) {
            return Flowable.just(entity)
        } else if (body.path.isNullOrEmpty() || !File(body.path!!).exists() || fileName.isNullOrBlank()) {
            return Flowable.create({
                it.onError(FileNotFoundException(entity.content))
            }, BackpressureStrategy.LATEST)
        } else {
            val isAssigned = IMCoreManager.getStorageModule()
                .isAssignedPath(body.path!!, fileName, format, entity.sid)
            // 如果不是指定的文件地址,需要先拷贝到im的目录下
            if (!isAssigned) {
                val desPath =
                    IMCoreManager.getStorageModule()
                        .allocLocalFilePath(entity.sid, fileName, format)
                val res = IMCoreManager.getStorageModule().copyFile(body.path!!, desPath)
                if (!res) {
                    return Flowable.create({
                        it.onError(FileNotFoundException())
                    }, BackpressureStrategy.LATEST)
                }
                // path 放入本地数据库
                body.path = desPath
                entity.content = Gson().toJson(body)
                updateDb(entity)
            }
            val key =
                IMCoreManager.getStorageModule()
                    .allocServerFilePath(entity.sid, entity.fUid, fileName)
            return Flowable.create({
                IMCoreManager.getFileLoaderModule()
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
                                    updateDb(entity)
                                    body.path = null // path不上传到服务器
                                    entity.content = Gson().toJson(body)
                                    it.onNext(entity)
                                }

                                LoadListener.Failed -> {
                                    it.onError(UploadException())
                                }

                                else -> {
                                    // 不用更新数据库，只用发送事件更新ui
                                    entity.extData =
                                        Gson().toJson(ImageBody.ExtData(state, progress))
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
        return "[图片]"
    }

}