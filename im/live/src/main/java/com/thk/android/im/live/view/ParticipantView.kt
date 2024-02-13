package com.thk.android.im.live.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.thk.android.im.live.room.BaseParticipant
import com.thk.android.im.live.room.LocalParticipant
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.live.R
import com.thk.im.android.live.databinding.ViewParticipantBinding
import kotlin.math.abs


class ParticipantView : ConstraintLayout {

    private var participant: BaseParticipant? = null
    private val binding: ViewParticipantBinding
    private var fullScreen = true // 默认最大化显示

    private var lastPositionX = 0f
    private var lastPositionY = 0f

    private var defaultScaleX = 0.35f
    private var defaultScaleY = 0.3f


    private var initPositionX = 0f
    private var initPositionY = 0f
    private var initTs = 0L


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_participant, this, true)
        binding = ViewParticipantBinding.bind(view)
        binding.btnVideoMuted.setOnClickListener {
            participant?.let {
                val muted = it.getVideoMuted()
                it.setVideoMuted(!muted)
            }
            setFullscreenMode(true)
            setSelected()
        }
        binding.btnAudioMuted.setOnClickListener {
            participant?.let {
                val muted = it.getAudioMuted()
                it.setAudioMuted(!muted)
            }
            setFullscreenMode(false)
            setSelected()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!fullScreen && event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastPositionX = event.x
                    lastPositionY = event.y
                    initPositionX = event.x
                    initPositionY = event.y
                    initTs = System.currentTimeMillis()
                }

                MotionEvent.ACTION_MOVE -> {
                    var newTransformX = (translationX + (event.x - lastPositionX) * defaultScaleX)
                    var newTransformY = (translationY + (event.y - lastPositionY) * defaultScaleY)
                    newTransformX =
                        newTransformX.coerceAtMost(AppUtils.instance().screenWidth * (1 - defaultScaleX) / 2)
                    newTransformY =
                        newTransformY.coerceAtMost(AppUtils.instance().screenHeight * (1 - defaultScaleY) / 2)
                    translationX =
                        newTransformX.coerceAtLeast(-AppUtils.instance().screenWidth * (1 - defaultScaleX) / 2)
                    translationY =
                        newTransformY.coerceAtLeast(-AppUtils.instance().screenHeight * (1 - defaultScaleY) / 2)
                }

                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - initTs < 500) {
                        if (abs(event.x-lastPositionX) < 20 && abs(event.y - lastPositionY) < 20) {
                            performClick()
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {}
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    fun setDefaultScale(scaleX: Float, scaleY: Float) {
        this.defaultScaleX = scaleX
        this.defaultScaleY = scaleY
    }

    fun isFullScreen(): Boolean {
        return fullScreen
    }

    fun setFullscreenMode(isFullScreen: Boolean) {
        if (this.fullScreen == isFullScreen) {
            return
        }
        this.fullScreen = isFullScreen
        if (isFullScreen) {
            switchToFullscreen()
        } else {
            switchToDragView()
        }
    }

    private fun switchToFullscreen() {
        val animators = AnimatorSet()
        val animatorTranslationX = ObjectAnimator.ofFloat(this, "translationX", 0f)
        val animatorTranslationY = ObjectAnimator.ofFloat(this, "translationY", 0f)
        val animatorScaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f)
        val animatorScaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f)
        animators.duration = 150
        animators.play(animatorTranslationX).with(animatorTranslationY).with(animatorScaleX)
            .with(animatorScaleY)
        animators.start()
        this.alpha = 1f
    }

    private fun switchToDragView() {
        val translationX = (AppUtils.instance().screenWidth * (1 - defaultScaleX)) / 2
        val translationY = (0 - AppUtils.instance().screenHeight * (1 - defaultScaleY)) / 2
        val scaleX = defaultScaleX
        val scaleY = defaultScaleY
        val animatorTranslationX = ObjectAnimator.ofFloat(this, "translationX", translationX)
        val animatorTranslationY = ObjectAnimator.ofFloat(this, "translationY", translationY)
        val animatorScaleX = ObjectAnimator.ofFloat(this, "scaleX", scaleX)
        val animatorScaleY = ObjectAnimator.ofFloat(this, "scaleY", scaleY)
        val animators = AnimatorSet()
        animators.duration = 150
        animators.play(animatorTranslationX).with(animatorTranslationY).with(animatorScaleX)
            .with(animatorScaleY)
        animators.start()
        this.alpha = 1f
    }


    private fun setSelected() {
        participant?.let {
            binding.btnVideoMuted.isSelected = !it.getVideoMuted()
            binding.btnAudioMuted.isSelected = !it.getAudioMuted()
        }
    }

    fun setParticipant(p: BaseParticipant) {
        if (participant != null) {
            participant!!.detachViewRender()
        }
        participant = p
        participant!!.attachViewRender(binding.rtcRendererVideo)
        participant!!.initPeerConn()
        setSelected()
    }

    fun startPeerConnection() {
        participant?.startPeerConnection()
    }

    fun isAudioMuted(): Boolean {
        return participant?.getAudioMuted() ?: false
    }

    fun muteAudio(muted: Boolean) {
        participant?.setAudioMuted(muted)
    }

    fun isVideoMuted(): Boolean {
        return participant?.getVideoMuted() ?: false
    }

    fun muteVideo(muted: Boolean) {
        participant?.setVideoMuted(muted)
    }

    fun switchCamera() {
        participant?.let {
            if (it is LocalParticipant) {
                it.switchCamera()
            }
        }
    }

    fun currentCamera(): Int {
        if (participant != null && participant!! is LocalParticipant) {
            return (participant!! as LocalParticipant).currentCamera()
        }
        return 0
    }

    fun getParticipant(): BaseParticipant? {
        return participant
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        participant?.attachViewRender(binding.rtcRendererVideo)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        participant?.detachViewRender()
    }

    fun destroy() {
        participant?.detachViewRender()
        participant = null
    }
}