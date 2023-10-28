package com.thk.im.android.media

import android.app.Activity
import android.app.Application
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.thk.im.android.base.LLog
import com.thk.im.android.base.compress.CompressUtils
import com.thk.im.android.core.IMFileFormat
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.media.audio.OggOpusRecorder
import com.thk.im.android.media.picker.GlideEngine
import com.thk.im.android.ui.manager.IMFile
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.IMContentResult
import com.thk.im.android.ui.protocol.IMMediaProvider
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

class MediaProvider(app: Application, token: String) : IMMediaProvider {

    init {
        OggOpusPlayer.initPlayer(app)
        OggOpusRecorder.initRecorder(app)
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
                                    val isGif = CompressUtils.isGif(it)
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