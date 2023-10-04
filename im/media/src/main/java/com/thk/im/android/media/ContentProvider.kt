package com.thk.im.android.media

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.view.View
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.thk.im.android.base.LLog
import com.thk.im.android.base.MediaUtils
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.media.audio.OggOpusRecorder
import com.thk.im.android.media.picker.GlideEngine
import com.thk.im.android.media.preview.MediaPreviewActivity
import com.thk.im.android.ui.manager.IMFile
import com.thk.im.android.ui.manager.MediaItem
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.IMContentProvider
import com.thk.im.android.ui.protocol.IMContentResult
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

class ContentProvider(app: Application) : IMContentProvider {

    init {
        OggOpusPlayer.initPlayer(app)
        OggOpusRecorder.initRecorder(app)
    }

    override fun preview(activity: Activity, items: ArrayList<MediaItem>, view: View) {
        val intent = Intent(activity, MediaPreviewActivity::class.java)
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        intent.putParcelableArrayListExtra("media_items", items)
        val rect = Rect(
            locations[0],
            locations[1],
            locations[0] + view.measuredWidth,
            locations[1] + view.measuredHeight,
        )
        intent.putExtra("origin_rect", rect)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }

    override fun openCamera(
        activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult
    ) {
        PictureSelector.create(activity).openCamera(SelectMimeType.ofAll())
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
            }).forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result, imContentResult)
                }

                override fun onCancel() {
                    imContentResult.onCancel()
                }
            })

    }

    override fun pick(
        activity: Activity, formats: List<IMFileFormat>, imContentResult: IMContentResult
    ) {
        PictureSelector.create(activity).openGallery(SelectMimeType.ofAll())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setCompressEngine(CompressFileEngine { context, source, call ->
                if (source != null && source.size > 0) {
                    Luban.with(context).load(source).ignoreBy(300)
                        .filter(object : CompressionPredicate {
                            override fun apply(path: String?): Boolean {
                                path?.let {
                                    val isGif = MediaUtils.isGif(it)
                                    return !isGif
                                }
                                return true
                            }
                        }).setCompressListener(object : OnNewCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(source: String?, compressFile: File?) {
                                call.onCallback(source, compressFile?.absolutePath)
                            }

                            override fun onError(source: String?, e: Throwable?) {
                                LLog.e("compress error $e")
                                call.onCallback(source, null)
                            }

                        }).launch()
                }
            }).isOriginalControl(true)
//            .setSelectorUIStyle())
            .isDisplayCamera(false).isGif(true).isPreviewImage(true).isWithSelectVideoImage(true)
            .isPreviewZoomEffect(true).isPreviewFullScreenMode(false).isPreviewVideo(true)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    onMediaResult(result, imContentResult)
                }

                override fun onCancel() {
                    imContentResult.onCancel()
                }
            })
    }

    override fun startRecordAudio(
        path: String, duration: Int, audioCallback: AudioCallback
    ): Boolean {
        return OggOpusRecorder.startRecord(path, duration, audioCallback)
    }

    override fun stopRecordAudio() {
        return OggOpusRecorder.stopRecording()
    }

    override fun isRecordingAudio(): Boolean {
        return OggOpusRecorder.isRecording()
    }

    override fun startPlayAudio(path: String, audioCallback: AudioCallback): Boolean {
        return OggOpusPlayer.startPlay(path, audioCallback)
    }

    override fun stopPlayAudio() {
        return OggOpusPlayer.stopPlaying()
    }

    override fun isPlayingAudio(): Boolean {
        return OggOpusPlayer.isPlaying()
    }

    private fun onMediaResult(
        result: List<LocalMedia>, imContentResult: IMContentResult
    ) {
        val files = mutableListOf<IMFile>()
        for (media in result) {
            var path = media.compressPath
            if (path == null || media.isOriginal) {
                path = media.realPath
            }
            val file = IMFile(path, media.mimeType)
            files.add(file)
        }
        imContentResult.onResult(files)
    }

}