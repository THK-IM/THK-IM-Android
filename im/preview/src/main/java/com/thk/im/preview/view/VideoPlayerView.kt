package com.thk.im.preview.view

import android.content.Context
import android.util.AttributeSet
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.thk.im.android.base.LLog
import com.thk.im.preview.VideoCache

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
        if (player == null) {
            player = ExoPlayer.Builder(context.applicationContext).build()
            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_READY -> {
                        }

                        Player.STATE_ENDED -> {
                        }

                        Player.STATE_IDLE -> {}
                        Player.STATE_BUFFERING -> {}
                    }
                }
            })
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        } else {
            player?.currentMediaItem?.localConfiguration?.uri?.path?.let {
                LLog.v("resume play $it $proxyUrl")
                if (it == proxyUrl.removePrefix("file://")) {
                    LLog.v("resume play")
                    player?.play()
                } else {
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.playWhenReady = true
                }
            }
        }
    }

    fun releasePlay() {
        player?.release()
        player = null
    }

    fun pause() {
        player?.pause()
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return false
    }
}