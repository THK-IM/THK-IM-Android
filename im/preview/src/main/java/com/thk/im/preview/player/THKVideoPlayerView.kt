package com.thk.im.preview.player

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.thk.im.android.preview.R
import com.thk.im.android.preview.databinding.LayoutVideoPlayerBinding
import com.thk.im.preview.VideoCache

class THKVideoPlayerView : RelativeLayout {


    private val binding: LayoutVideoPlayerBinding

    private var attached = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        val view = LayoutInflater.from(context).inflate(
            R.layout.layout_video_player, this, true
        )
        binding = LayoutVideoPlayerBinding.bind(view)
    }

    init {
        setupPlayer()
        setupEvent()
    }

    private fun setupPlayer() {
        binding.playerView.clipChildren = true
        binding.playerView.player = ExoPlayer.Builder(context).build()
        binding.playerView.player?.playWhenReady = true
        binding.playerView.useController = false
        binding.playerView.setBackgroundColor(Color.BLACK)
        binding.playerView.player?.volume = 0f
        binding.bottomController.setMuted(true)
    }

    fun setVolume(volume: Float) {
        binding.playerView.player?.volume = volume
        binding.bottomController.setMuted(volume <= 0f)
    }

    private fun setupEvent() {
        binding.playerView.player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    updatePlayTime()
                }
                binding.middleController.showLoading(playbackState == Player.STATE_BUFFERING)
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                binding.middleController.showLoading(isLoading)
            }
        })

        binding.bottomController.setSeekbarDragListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.bottomController.setItemClickListener {
            if (it.id == R.id.iv_mute) {
                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    binding.playerView.player?.volume = 0f
                } else {
                    binding.playerView.player?.volume = 1f
                }
            } else if (it.id == R.id.iv_play_pause) {
                it.isSelected = !it.isSelected
                if (it.isSelected) {
                    binding.playerView.player?.pause()
                } else {
                    binding.playerView.player?.play()
                }
            }
        }
    }

    private fun seekTo(progress: Int) {
        val player = binding.playerView.player ?: return
        player.seekTo(player.duration * progress / 100)
    }

    private fun updatePlayTime() {
        val player = binding.playerView.player ?: return
        if (!attached) return
        if (player.duration < 0) {
            val total = (player.duration / 1000)
            val buffered = player.bufferedPosition / 1000
            val current = player.currentPosition / 1000
            Log.v("Player", "$total, $buffered, $current ")
            binding.bottomController.updateTime(total, buffered, current)
        }
        binding.playerView.postDelayed({
            updatePlayTime()
        }, 200)
    }

    private fun resumePlay() {
        if (attached) {
            binding.playerView.player?.play()
        }
    }

    private fun pausePlay() {
        if (attached) {
            binding.playerView.player?.pause()
        }
    }

    fun attachToParent(parent: ViewGroup) {
        if (parent != this.parent) {
            detachFromParent()
            val lp = LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
            )
            parent.addView(this, lp)
            parent.visibility = View.VISIBLE
        }
        attached = true
    }

    fun playWithUrl(url: String) {
        val proxyUrl: String = if (url.startsWith("http")) {
            VideoCache.getProxy().getProxyUrl(url)
        } else {
            url
        }
        binding.bottomController.updateTime(0, 0, 0)
        binding.playerView.player?.setMediaItem(MediaItem.fromUri(Uri.parse(proxyUrl)))
        binding.playerView.player?.prepare()
    }

    fun isPlaying(): Boolean {
        return binding.playerView.player?.isPlaying ?: false
    }

    fun replay() {
        binding.playerView.player?.seekTo(0)
    }


    private fun detachFromParent() {
        (this.parent as? ViewGroup)?.visibility = View.GONE
        (this.parent as? ViewGroup)?.removeView(this)
        attached = false
    }

    fun stopPlay() {
        binding.playerView.player?.stop()
        detachFromParent()
    }

    fun onResume() {
        resumePlay()
    }

    fun onPause() {
        pausePlay()
    }

    fun onDestroy() {
        stopPlay()
        binding.playerView.player?.release()
    }

    fun setupUI(hideSeekbar: Boolean, hidePlayPause: Boolean, hideTotalTime: Boolean) {
        binding.bottomController.hideSeekBar(hideSeekbar)
        binding.bottomController.hidePlayButton(hidePlayPause)
        binding.bottomController.hideTotalTime(hideTotalTime)
    }

    fun hideControllers(hideController: Boolean) {
        binding.bottomController.visibility = if (hideController) View.GONE else View.VISIBLE
//        binding.middleController.visibility = if (hideController) View.GONE else View.VISIBLE
    }

}