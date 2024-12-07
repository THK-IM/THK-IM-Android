package com.thk.im.android.live.player

import android.content.res.AssetFileDescriptor
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.thk.im.android.live.engine.PCMTransUtil
import java.io.FileDescriptor
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque

class RTCMediaPlayer : IRTCMediaPlayer, DecodeCallback {

    private var callback: IRTCMediaPlayerCallback? = null
    private var currentDecodeThread: DecodeThread? = null
    private var numChannels = 2
    private var sampleRateHz = 48000
    private val pcmBufferQueue = LinkedBlockingDeque<ByteArray>()
    private var resample: PcmResample? = null
    private var remainByteArray: ByteArray? = null

    override fun setPlayCallback(callback: IRTCMediaPlayerCallback?) {
        this.callback = callback
    }

    override fun updateRTCParams(numChannels: Int, sampleRateHz: Int) {
        this.numChannels = numChannels
        this.sampleRateHz = sampleRateHz
    }

    override fun fetchRTCPCM(len: Int): ByteArray? {
        if (remainByteArray != null) {
            if (remainByteArray!!.size > len) {
                val rsp = ByteArray(len)
                System.arraycopy(remainByteArray!!, 0, rsp, 0, len)
                val remain = ByteArray(remainByteArray!!.size - len)
                System.arraycopy(remainByteArray!!, len, remain, 0, remain.size)
                remainByteArray = remain
                return rsp
            } else if (remainByteArray!!.size == len) {
                val rsp = ByteArray(len)
                System.arraycopy(remainByteArray!!, 0, rsp, 0, len)
                remainByteArray = null
                return rsp
            }
        }
        val rsp = ByteArray(len)
        var copiedLen = 0
        remainByteArray?.let {
            System.arraycopy(it, 0, rsp, 0, it.size)
            copiedLen = it.size
        }
        while (true) {
            val needLen = len - copiedLen
            val buffer = pcmBufferQueue.pollFirst()
            if (buffer == null) {
                if (rsp.isNotEmpty()) {
                    remainByteArray = rsp
                }
                return null
            }
            if (buffer.size > needLen) {
                System.arraycopy(buffer, 0, rsp, copiedLen, needLen)
                val remain = ByteArray(buffer.size - needLen)
                System.arraycopy(buffer, needLen, remain, 0, remain.size)
                remainByteArray = remain
                return rsp
            } else if (buffer.size == needLen) {
                System.arraycopy(buffer, 0, rsp, copiedLen, needLen)
                remainByteArray = null
                return rsp
            } else {
                System.arraycopy(buffer, 0, rsp, copiedLen, buffer.size)
                copiedLen += buffer.size
            }
        }
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
        currentDecodeThread?.initAudioTrack(numChannels, sampleRateHz)
//        Thread {
//            run {
//                while (true) {
//                    fetchRTCPCM(100)?.let { buffer ->
//                        currentDecodeThread?.playPCM(buffer)
//                    }
//                }
//            }
//        }.start()
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
        if (resample != null && !resample!!.support(
                channelNum,
                sampleRate,
                this.numChannels,
                this.sampleRateHz,
            )
        ) {
            resample?.flush()
            resample?.close()
            resample = null
        }
        if (resample == null) {
            resample = PcmResample()
            pcmBufferQueue.clear()
            resample?.init(channelNum, sampleRate, this.numChannels, this.sampleRateHz)
        }
        resample?.let {
            it.feedData(ba, ba.size)
            val outSize = it.convertedSize
            if (outSize > 0) {
                val outBuffer = ByteArray(outSize)
                it.receiveConvertedData(outBuffer)
                pcmBufferQueue.add(outBuffer)
            }
        }
    }

    fun mixBuffer(
        byteBuffer: ByteBuffer,
        format: Int,
        channelCount: Int,
        sampleRate: Int,
        bytesRead: Int
    ) {

        Log.d(
            "RTCMediaPlayer",
            "$format, $channelCount, $sampleRate, $bytesRead, ${byteBuffer.remaining()}"
        )
        byteBuffer.clear()
        byteBuffer.rewind()
        val mediaPCMData = fetchRTCPCM(bytesRead) ?: return
//        val originData = ByteArray(bytesRead)
//        byteBuffer.get(originData)
//        for (i in originData.indices) {
//            originData[i] = (originData[i] + mediaPCMData[i]).toByte()
//        }
//        byteBuffer.clear()
//        byteBuffer.put(originData)
        byteBuffer.put(mediaPCMData)
//        PCMTransUtil.averageMix(originData, mediaPCMData)?.let {
//            byteBuffer.clear()
//            byteBuffer.put(it)
//        }
    }

}