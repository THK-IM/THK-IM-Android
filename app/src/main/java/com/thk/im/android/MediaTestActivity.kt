package com.thk.im.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.base.LLog
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.databinding.ActivityMediaTestBinding
import com.thk.im.android.media.audio.AudioCallback
import com.thk.im.android.media.audio.AudioStatus
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.media.audio.OggOpusRecorder
import java.io.File

class MediaTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaTestBinding
    private lateinit var tmpAudioPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OggOpusRecorder.initRecorder(application)
        binding = ActivityMediaTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()
        tmpAudioPath = cacheDir.absolutePath + "/tmp_opus.ogg"
        LLog.d("Path: $tmpAudioPath")
        binding.btnRecord.setOnClickListener {
            if (OggOpusRecorder.isRecording()) {
                OggOpusRecorder.stopRecording()
            } else {
                val file = File(tmpAudioPath)
                if (file.exists()) {
                    file.delete()
                }
                file.createNewFile()
                val success = OggOpusRecorder.startRecord(tmpAudioPath, object : AudioCallback {
                    override fun notify(path: String, second: Int, db: Double, state: AudioStatus) {
                        LLog.d("recorder notify $path, $second, $db, $state")
                        if (state == AudioStatus.Finished || state == AudioStatus.Exited) {
                            binding.btnRecord.text = "start record"
                        }
                    }

                })
                if (success) {
                    binding.btnRecord.text = "stop record"
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            if (OggOpusPlayer.isPlaying()) {
                OggOpusPlayer.stopPlaying()
                binding.btnPlay.text = "start play"
            } else {
                val file = File(tmpAudioPath)
                if (file.exists()) {
                    val success = OggOpusPlayer.startPlay(tmpAudioPath, object : AudioCallback {
                        override fun notify(
                            path: String,
                            second: Int,
                            db: Double,
                            state: AudioStatus
                        ) {
                            LLog.d("player notify $path, $second, $db, $state")
                            if (state == AudioStatus.Finished || state == AudioStatus.Exited) {
                                binding.btnPlay.text = "start play"
                            }
                        }
                    })
                    if (success) {
                        binding.btnPlay.text = "stop play"
                    }
                }
            }
//            if (OpusPlayer.getInstance().isPlaying) {
//                OpusPlayer.getInstance().stop()
//                binding.btnPlay.text = "start play"
//            } else {
//                val file = File(tmpAudioPath)
//                if (file.exists()) {
//                    OpusPlayer.getInstance().play(tmpAudioPath)
//                    binding.btnPlay.text = "stop play"
//                }
//            }
        }
    }

    private fun startRecord() {
        val file = File(tmpAudioPath)
        if (file.exists()) {
            if (file.isFile) {
                LLog.d("File size: ${file.length()}")
            }
            file.delete()
        }
    }

    private fun stopRecord() {
    }

    private fun onStopped() {
        binding.btnRecord.text = "start record"
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {

                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                    ToastUtils.show("permission denied")
                    finish()
                }
            })
    }
}