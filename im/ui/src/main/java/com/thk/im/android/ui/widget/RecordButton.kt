package com.thk.im.android.ui.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.utils.ToastUtils
import com.thk.im.android.ui.R
import com.thk.im.android.ui.voice.IRecorder
import com.thk.im.android.ui.voice.RecordCallback
import kotlin.math.abs

/**
 * 录制按钮
 */
class RecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle,
) : AppCompatButton(context, attrs, defStyleAttr), RecordCallback {

    private var v1: Float = 0f
    private var v2: Float = 0f

    private var mRecorder: IRecorder? = null

    var recordListener: RecordListener? = null

    fun setRecorder(recorder: IRecorder, maxDuration: Int) {
        mRecorder = recorder
        mRecorder?.setRecordCallback(this)
        mRecorder?.setMaxDuration(maxDuration)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isPermissionsGranted(context, Permission.RECORD_AUDIO)) {
            if (event?.action == MotionEvent.ACTION_DOWN) {
                v1 = event.y
                setBackgroundResource(R.drawable.shape_edt_round_press)
                text = context.getString(R.string.release_to_send_voice)
                mRecorder?.startRecord()
                showVolumeDialog()
            } else if (event?.action == MotionEvent.ACTION_MOVE) {
                v2 = event.y
                if (abs(v1 - v2) > 180) {
                    showCancelRecord(true)
                } else {
                    showCancelRecord(false)
                }
            } else if (event?.action == MotionEvent.ACTION_UP) {
                setBackgroundResource(R.drawable.shape_edt_round)
                text = context.getString(R.string.tip_for_voice_forward)
                mRecorder?.stopRecord()
                hideVolumeDialog()
            }
            return true
        } else {
            requestPermissions(Permission.RECORD_AUDIO)
            return true
        }
    }


    private fun isPermissionsGranted(context: Context, vararg permission: String): Boolean {
        return XXPermissions.isGranted(
            context,
            permission
        )
    }

    private fun requestPermissions(vararg permission: String) {
        XXPermissions.with(context)
            .permission(permission)
            .request { _, all ->
                if (!all) {
                    Toast.makeText(
                        context,
                        R.string.plz_get_premission_and_send_voice,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


    private var soundVolumeDialog: Dialog? = null

    private fun showVolumeDialog() {
        if (soundVolumeDialog == null) {
            soundVolumeDialog = Dialog(context, R.style.SoundVolumeStyle)
            soundVolumeDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            soundVolumeDialog?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            soundVolumeDialog?.setContentView(R.layout.dialog_sound_volume)
            soundVolumeDialog?.setCanceledOnTouchOutside(false)
        }

        val ivSoundVolume = soundVolumeDialog?.findViewById<ImageView>(R.id.iv_sound_volume)
        val llSoundVolume = soundVolumeDialog?.findViewById<LinearLayout>(R.id.ll_sound_volume)
        ivSoundVolume?.visibility = View.VISIBLE
        llSoundVolume?.setBackgroundResource(R.drawable.tt_sound_volume_default_bk)
        soundVolumeDialog?.show()
    }

    private var showCancel = false
    private fun showCancelRecord(showCancel: Boolean) {
        val ivSoundVolume = soundVolumeDialog?.findViewById<ImageView>(R.id.iv_sound_volume) // 录音动画
        val llSoundVolume = soundVolumeDialog?.findViewById<LinearLayout>(R.id.ll_sound_volume)
        ivSoundVolume?.visibility = View.VISIBLE
        if (showCancel) {
            llSoundVolume?.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk)
        } else {
            llSoundVolume?.setBackgroundResource(R.drawable.tt_sound_volume_default_bk)
        }
        this.showCancel = showCancel
    }

    // 根据分贝值设置录音时的音量动画
    private fun onReceiveMaxVolume(voiceValue: Int) {
        val ivSoundVolume = soundVolumeDialog?.findViewById<ImageView>(R.id.iv_sound_volume) // 录音动画
        ivSoundVolume?.post {
            if (voiceValue < 200.0) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_01)
            } else if (voiceValue > 200.0 && voiceValue < 600) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_02)
            } else if (voiceValue > 600.0 && voiceValue < 1200) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_03)
            } else if (voiceValue > 1200.0 && voiceValue < 2400) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_04)
            } else if (voiceValue > 2400.0 && voiceValue < 10000) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_05)
            } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_06)
            } else if (voiceValue > 28000.0) {
                ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_07)
            }
        }

    }

    private fun hideVolumeDialog() {
        if (soundVolumeDialog?.isShowing == true) {
            soundVolumeDialog?.dismiss()
        }
    }

    override fun onDetachedFromWindow() {
        hideVolumeDialog()
        super.onDetachedFromWindow()
    }

    interface RecordListener {
        /**
         * 录制完成的回调
         * @param recorder recorder
         * @param duration 录制时长
         */
        fun onRecordFinish(recorder: IRecorder, duration: Int)

        /**
         * 录制取消
         * @param recorder recorder
         */
        fun onRecordCancel(recorder: IRecorder)
    }

    override fun onVolumeChange(recorder: IRecorder, volume: Int) {
        onReceiveMaxVolume(volume)
    }

    override fun onProcess(recorder: IRecorder, duration: Int) {

    }

    override fun onRecordFinish(recorder: IRecorder, duration: Int) {
        hideVolumeDialog()
        if (duration < 1f) {
            ToastUtils.showToast(context, context.getString(R.string.record_too_short))
            return
        }
        recordListener?.onRecordFinish(recorder, duration)
    }
}