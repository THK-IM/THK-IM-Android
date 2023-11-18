package com.thk.im.android

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.ToastUtils
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

        binding.etRoom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isNotEmpty()) {
                    binding.buttonThird.text = "进入" + charSequence + "房间"
                } else {
                    binding.buttonThird.text = "创建房间"
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        binding.buttonThird.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.setClass(this, WebRtcActivity::class.java)
            if (binding.etRoom.text.toString().isNotEmpty()) {
                val roomId: String = binding.etRoom.text.toString()
                intent.putExtra("room_id", roomId)
            }
            startActivity(intent)
        })
    }

    private fun checkPermission() {
        XXPermissions.with(this).permission(Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {}

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                    com.thk.im.android.core.base.utils.ToastUtils.show("permission denied")
                    finish()
                }

            })
    }
}