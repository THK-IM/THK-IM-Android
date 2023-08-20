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
import com.thk.im.android.media.picker.AlbumStyleUtils
import com.thk.im.android.media.picker.GlideEngine
import com.thk.im.android.ui.panel.component.internal.BaseMediaComponent
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File


/**
 * 相册
 */
class PhotoAlbumComponent(name: String, @DrawableRes id: Int) :
    BaseMediaComponent(name, id) {
    
    private lateinit var imageMsgProcessor: ImageMsgProcessor
    override fun onComponentCreate() {
        imageMsgProcessor = IMManager.getMessageModule()
            .getMessageProcessor(MsgType.IMAGE.value) as ImageMsgProcessor
    }

    override fun onComponentClick(view: View?) {
        requestPermissions(Permission.READ_EXTERNAL_STORAGE)
    }

    override fun onPermissionGranted(permissions: List<String>) {
        selectImage(context)
    }

    private fun selectImage(ctx: Context) {
        PictureSelector.create(ctx).openGallery(SelectMimeType.ofAll())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source != null && source.size > 0) {
                    Luban.with(context).load(source).ignoreBy(300)
                        .filter(object : CompressionPredicate {
                            override fun apply(path: String?): Boolean {
                                path?.let {
                                    val isGif = com.thk.im.android.common.MediaUtils.isGif(it)
                                    return !isGif
                                }
                                return true
                            }
                        })
                        .setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call.onCallback(source, compressFile?.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                e?.printStackTrace()
                                call.onCallback(source, null)
                            }

                        }).launch()
                }
            })
            .isOriginalControl(true)
            .setSelectorUIStyle(AlbumStyleUtils.getStyle(ctx))
            .isDisplayCamera(false)
            .isGif(true)
            .isPreviewImage(true)
            .isWithSelectVideoImage(true)
            .isPreviewZoomEffect(true)
            .isPreviewFullScreenMode(false)
            .isPreviewVideo(true)
            .forResult(object :
                OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result)
                }

                override fun onCancel() {
                }

            })
    }

}