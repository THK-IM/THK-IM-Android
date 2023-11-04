package com.thk.im.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.base.LLog
import com.thk.im.android.base.utils.ToastUtils
import com.thk.im.android.databinding.ActivityMediaTestBinding
import com.thk.im.android.media.audio.OggOpusPlayer
import com.thk.im.android.media.audio.OggOpusRecorder
import com.thk.im.android.ui.protocol.AudioCallback
import com.thk.im.android.ui.protocol.AudioStatus
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
                val success =
                    OggOpusRecorder.startRecord(tmpAudioPath, 60 * 1000, object : AudioCallback {
                        override fun audioData(
                            path: String,
                            second: Int,
                            db: Double,
                            state: AudioStatus
                        ) {
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

                        override fun audioData(
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
        }

        binding.videoPlay.setOnClickListener {
            val intent = Intent(this@MediaTestActivity, VideoActivity::class.java)
            startActivity(intent)
        }
    }
    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {}

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                    ToastUtils.show("permission denied")
                    finish()
                }

            })
    }
}