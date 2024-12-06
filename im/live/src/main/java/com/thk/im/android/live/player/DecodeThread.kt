package com.thk.im.android.live.player

import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.thk.im.android.core.base.LLog
import java.io.FileDescriptor
import java.nio.ByteBuffer

interface DecodeCallback {
    fun onError(t: Throwable)
    fun onBuffer(channelNum: Int, sampleRate: Int, ba: ByteArray)
}

class DecodeThread(private val callback: DecodeCallback) : Thread() {

    @Volatile
    private var isPlaying: Boolean = false
    private var isReleased: Boolean = false
    private var started: Boolean = false

    private val extractMimeType = "audio/"
    private var extractor: MediaExtractor? = null
    private var mediaCodec: MediaCodec? = null
    private var trackFormat: MediaFormat? = null
    private var audioTrack: AudioTrack? = null

    @RequiresApi(Build.VERSION_CODES.N)
    fun setDataSource(afd: AssetFileDescriptor) {
        try {
            extractor = MediaExtractor()
            extractor?.setDataSource(afd)
        } catch (e: Exception) {
            callback.onError(e)
        }
        initDataSource()
    }

    fun setDataSource(fd: FileDescriptor) {
        try {
            extractor = MediaExtractor()
            extractor?.setDataSource(fd)
        } catch (e: Exception) {
            callback.onError(e)
        }
        initDataSource()
    }

    fun setDataSource(path: String) {
        try {
            extractor = MediaExtractor()
            extractor?.setDataSource(path)
        } catch (e: Exception) {
            callback.onError(e)
        }
        initDataSource()
    }

    fun play() {
        LLog.d("RTCMediaPlayer", "play: $isPlaying")
        isPlaying = true
        if (!started) {
            super.start()
            started = true
        }
    }

    fun pausePlay() {
        isPlaying = false
    }

    fun resumePlay() {
        isPlaying = true
    }

    fun stopPlay() {
        isPlaying = false
        isReleased = true
    }

    fun seekTo(time: Long) {
        extractor?.seekTo(1000 * time, MediaExtractor.SEEK_TO_NEXT_SYNC)
    }


    override fun start() {
    }

    override fun run() {
        super.run()
        val extractor = this.extractor ?: return
        val mediaCodec = this.mediaCodec ?: return
        val format = this.trackFormat ?: return
        while (!isReleased) {
            sleep(200)
            decode(format, extractor, mediaCodec)
        }
        release()
    }


    private fun initDataSource() {
        try {
            // 媒体文件中的轨道数量 （一般有视频，音频，字幕等）
            val trackCount: Int = extractor?.trackCount ?: 0
            // 记录轨道索引id，MediaExtractor 读取数据之前需要指定分离的轨道索引
            // 视频轨道格式信息
            for (i in 0 until trackCount) {
                val format = extractor?.getTrackFormat(i)
                val mimeType = format?.getString(MediaFormat.KEY_MIME) ?: ""
                if (mimeType.startsWith(extractMimeType)) {
                    trackFormat = format
                    extractor?.selectTrack(i)
                    break
                }
            }
            if (trackFormat != null) {
                val audioChannels = trackFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                val audioSampleRate = trackFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//                initAudioTrack(audioChannels, audioSampleRate)
                val formatType = trackFormat?.getString(MediaFormat.KEY_MIME) ?: ""
                mediaCodec = MediaCodec.createDecoderByType(formatType)
                mediaCodec?.configure(trackFormat, null, null, 0)
                mediaCodec?.start()
                isReleased = false
            } else {
                callback.onError(Throwable("Media not Support"))
            }
        } catch (e: Exception) {
            callback.onError(e)
        }
    }

    fun initAudioTrack(audioChannels: Int, audioSampleRate: Int) {
        val channelConfig =
            if (audioChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        val minBuffSize = AudioTrack.getMinBufferSize(
            audioSampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )
        // 配置配置AudioTrack
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(audioSampleRate)
                        .setChannelMask(channelConfig)
                        .build()
                )
                .setBufferSizeInBytes(minBuffSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }
        audioTrack?.play()
    }

    fun playPCM(pcmData: ByteArray) {
        audioTrack?.write(pcmData, 0, pcmData.size)
    }

    private fun decode(format: MediaFormat, extractor: MediaExtractor, mediaCodec: MediaCodec) {
        LLog.d("RTCMediaPlayer", "decode: start")
        val bufferInfo = MediaCodec.BufferInfo()
        val channelNum = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        try {
            val isEOS = false
            while (isPlaying) {
                val timeout: Long = 10 * 1000 // 1ms
                if (!isEOS) {
                    // 获取可用的输入缓冲区的索引
                    val inputIndex = mediaCodec.dequeueInputBuffer(timeout)
                    if (inputIndex > 0) {
                        // 获取输入缓冲区，并向缓冲区写入数据
                        val inputBuffer = mediaCodec.getInputBuffer(inputIndex)
                        if (inputBuffer != null) {
                            inputBuffer.clear()
                        } else {
                            continue
                        }
                        // 从流中读取的采用数据的大小
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            mediaCodec.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            break
                        } else {
                            mediaCodec.queueInputBuffer(
                                inputIndex,
                                0,
                                sampleSize,
                                0,
                                0
                            )
                            extractor.advance()
                        }
                    }
                }

                // 获取已成功编解码的输出缓冲区的索引
                var outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeout)
                var outputBuffer: ByteBuffer?
                while (outputIndex >= 0 && isPlaying) {
                    outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
                    outputBuffer?.let {
                        val pcmData = ByteArray(bufferInfo.size)
                        it.get(pcmData)
                        LLog.d("RTCMediaPlayer", "pcmData: ${pcmData.size}")
                        it.clear()
                        if (pcmData.isNotEmpty()) {
                            callback.onBuffer(channelNum, sampleRate, pcmData)
                        }
                    }
                    // 释放
                    mediaCodec.releaseOutputBuffer(outputIndex, false)
                    // 再次获取数据
                    outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, timeout)
                }
            }
        } catch (e: Exception) {
            isReleased = true
            callback.onError(e)
        }
    }

    private fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        mediaCodec?.stop()
        mediaCodec?.release()
        extractor?.release()
        mediaCodec = null
        extractor = null
        isReleased = true
    }
}