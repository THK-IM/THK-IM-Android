package com.thk.im.android.live.engine

import com.thk.im.android.core.base.LLog
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioCaptureProxy : AudioProcessing {

    // int sampleRateHz, int numChannels
    override fun initialize(p0: Int, p1: Int) {
        LLog.d("LiveAudioProxy", "Capture initialize $p0, $p1")
//        LiveRTCEngine.shared().mediaPlayer?.updateRTCParams(p1, p0)
    }

    override fun reset(p0: Int) {
        LLog.d("LiveAudioProxy", "Capture reset $p0")
    }

    fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
        // 每个采样点由2个字节组成
        val sampleCount = byteArray.size / 2
        val floatArray = FloatArray(sampleCount)

        for (i in 0 until sampleCount) {
            // 低字节和高字节
            val low = byteArray[i * 2].toInt() and 0xFF // 无符号低位
            val high = byteArray[i * 2 + 1].toInt() // 有符号高位
            var sample = (high shl 8) or low // 合并成short
            if (sample > 32767) {
                sample -= 65536 // 处理符号扩展
            }

            // 归一化到 [-1.0f, 1.0f]
            floatArray[i] = sample / 32768.0f
        }
        return floatArray
    }

    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(p0: Int, p1: Int, p2: ByteBuffer) {
//        val mediaPlayer = LiveRTCEngine.shared().mediaPlayer ?: return
//        val mediaPCMData = mediaPlayer.fetchRTCPCM(p0 * p1) ?: return
//        LLog.d("LiveAudioProxy", "process $p0 $p1 ${p2.limit()}  ")
//        p2.clear()
//        p2.put(mediaPCMData)
//        val floatBuffer = p2.asFloatBuffer()
//        for (i in 0 until floatBuffer.limit()) {
//            Log.v("LiveAudioProxy", floatBuffer.get(i).toString())
//        }
//        val floatBuffer = p2.asFloatBuffer()
//        val mediaPlayer = LiveRTCEngine.shared().mediaPlayer ?: return
//        val mediaPCMData = mediaPlayer.fetchRTCPCM(p1 * 2) ?: return
//        val fPcm = byteArrayToFloatArray(mediaPCMData)
//        for (i in fPcm.indices) {
//            fPcm[i] += floatBuffer.get(i)
//        }
//        floatBuffer.clear()
//        floatBuffer.put(fPcm)

//        LLog.d("LiveAudioProxy", "process $p0 $p1 ${floatBuffer.limit()}  ")
////        val origin = FloatArray(p1)
////        for (i in 0 until  p1) {
////            origin[i] = p2.getFloat(i)
////        }
//        val pcm = byteArrayToFloatArray(mediaPCMData)
//        floatBuffer.clear()
//        floatBuffer.put(pcm)
//        floatBuffer.flip()
//        LLog.d("LiveAudioProxy", "process $p0 $p1 ${p2.limit()} ${mediaPCMData.size} ")
//        p2.put(mediaPCMData)
//        p2.flip()
//        val mixPcm = PCMTransUtil.averageMix(arrayOf(pcmData, mediaPCMData))
//        p2.rewind()
//        p2.put(mixPcm)
    }
}