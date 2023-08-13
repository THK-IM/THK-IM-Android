package com.thk.im.android.ui.panel.component

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import com.hjq.permissions.Permission
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.thk.im.android.core.IMManager
import com.thk.im.android.core.processor.ImageMsgProcessor
import com.thk.im.android.db.entity.MsgType
import com.thk.im.android.ui.panel.component.internal.BaseMediaComponent
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


/**
 * 拍照
 */
class CameraComponent(name: String, @DrawableRes id: Int) :
    BaseMediaComponent(name, id) {

    private lateinit var imageMsgProcessor: ImageMsgProcessor


    override fun onComponentCreate() {
        imageMsgProcessor = IMManager.getMessageModule()
            .getMessageProcessor(MsgType.IMAGE.value) as ImageMsgProcessor
    }


    override fun onComponentDestroy() {

    }

    override fun onComponentClick(view: View?) {
        requestPermissions(Permission.READ_EXTERNAL_STORAGE, Permission.CAMERA)
    }

    override fun onPermissionDenied(permissions: List<String>) {
        super.onPermissionDenied(permissions)
    }

    override fun onPermissionGranted(permissions: List<String>) {
        cameraMedia(context)
    }

    private fun cameraMedia(ctx: Context) {
        PictureSelector.create(ctx)
            .openCamera(SelectMimeType.ofAll())
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source != null && source.size > 0) {
                    Luban.with(context).load(source).ignoreBy(300)
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call.onCallback(source, compressFile?.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                call.onCallback(source, null)
                            }

                        }).launch()
                }
            })
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result)
                }

                override fun onCancel() {
                }
            })
    }


}