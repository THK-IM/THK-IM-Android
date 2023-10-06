package com.thk.im.android.media.preview.view

import android.content.Context
import android.util.AttributeSet
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.thk.im.android.media.preview.VideoCache

class VideoPlayerView : PlayerView {
    @UnstableApi
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        setShowNextButton(false)
        setShowRewindButton(false)
        setShowPreviousButton(false)
        setShowFastForwardButton(false)
        controllerAutoShow = true
        controllerHideOnTouch = false
    }

    fun startPlay(url: String) {
        val proxyUrl: String = if (url.startsWith("http")) {
            VideoCache.getProxy().getProxyUrl(url)
        } else {
            url
        }
        val mediaItem = MediaItem.fromUri(proxyUrl)
        player = ExoPlayer.Builder(context.applicationContext).build()
        player!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        player!!.play()
                    }

                    Player.STATE_ENDED -> {
                    }

                    Player.STATE_IDLE -> {}
                    Player.STATE_BUFFERING -> {}
                }
            }
        })
        player!!.setMediaItem(mediaItem)
        player!!.prepare()
        player!!.playWhenReady = true
    }

    fun stopPlay() {
        player!!.release()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player!!.pause()
    }

    override fun onPause() {
        super.onPause()
        player!!.pause()
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return false
    }
}