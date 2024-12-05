package com.thk.im.android.live.player

import android.content.res.AssetFileDescriptor
import android.os.Build
import androidx.annotation.RequiresApi
import com.thk.im.android.core.base.LLog
import java.io.FileDescriptor
import java.lang.Thread.sleep
import java.util.concurrent.LinkedBlockingDeque

class RTCMediaPlayer : IRTCMediaPlayer, DecodeCallback {

    private var callback: IRTCMediaPlayerCallback? = null
    private var currentDecodeThread: DecodeThread? = null
    private var numChannels = 1
    private var sampleRateHz = 48000
    private val pcmBufferQueue = LinkedBlockingDeque<ByteArray>()
    private var resample: PcmResample? = null

    override fun setPlayCallback(callback: IRTCMediaPlayerCallback?) {
        this.callback = callback
    }

    override fun updateRTCParams(numChannels: Int, sampleRateHz: Int) {
        this.numChannels = numChannels
        this.sampleRateHz = sampleRateHz
    }

    override fun fetchRTCPCM(len: Int): ByteArray? {
        return pcmBufferQueue.pollFirst()
    }

    override fun setMediaItem(path: String) {
        if (currentDecodeThread != null) {
            currentDecodeThread?.stopPlay()
        }
        currentDecodeThread = DecodeThread(this)
        currentDecodeThread?.setDataSource(path)
    }

    override fun setMediaItem(fd: FileDescriptor) {
        if (currentDecodeThread != null) {
            currentDecodeThread?.stopPlay()
        }
        currentDecodeThread = DecodeThread(this)
        currentDecodeThread?.setDataSource(fd)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun setMediaItem(afd: AssetFileDescriptor) {
        if (currentDecodeThread != null) {
            currentDecodeThread?.stopPlay()
        }
        currentDecodeThread = DecodeThread(this)
        currentDecodeThread?.setDataSource(afd)
    }

    override fun pause() {
        currentDecodeThread?.pausePlay()
    }

    override fun play() {
        currentDecodeThread?.play()
    }

    override fun seekTo(time: Long) {
        currentDecodeThread?.seekTo(time)
    }

    override fun release() {
        currentDecodeThread?.stopPlay()
        resample?.flush()
        resample?.close()
    }

    override fun onError(t: Throwable) {
        callback?.onPlayerError(t)
    }

    override fun onBuffer(channelNum: Int, sampleRate: Int, ba: ByteArray) {
        if (resample!!.support(this.numChannels, this.sampleRateHz, channelNum, sampleRate)) {
            resample?.flush()
            resample?.close()
            resample = null
        }
        if (resample == null) {
            resample = PcmResample()
            resample?.init(this.numChannels, this.sampleRateHz, channelNum, sampleRate)
        }
        resample?.let {
            it.feedData(ba, ba.size)
            val outSize = it.convertedSize
            val outBuffer = ByteArray(outSize)
            it.receiveConvertedData(outBuffer)
            pcmBufferQueue.add(outBuffer)
        }
        val bufferSize = pcmBufferQueue.size
        LLog.d("RTCMediaPlayer", "bufferSize: $bufferSize")
        while (bufferSize > 10 * sampleRate) {
            sleep(100)
        }
    }

}