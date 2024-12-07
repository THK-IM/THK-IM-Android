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

    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(p0: Int, p1: Int, p2: ByteBuffer) {
//        val mediaPlayer = LiveRTCEngine.shared().mediaPlayer ?: return
//        val mediaPCMData = mediaPlayer.fetchRTCPCM(p2.limit()) ?: return
//        val originData = ByteArray(p2.limit())
//        p2.get(originData)
//        PCMTransUtil.averageMix(originData, mediaPCMData)?.let {
//            p2.clear()
//            p2.put(it)
//        }
//        val newData = ByteArray(p2.limit())
//        for (i in newData.indices) {
//            if (i % 2 != 0) {
//                newData[i] = (originData[i] + mediaPCMData[i]).toByte()
//            }
//        }

    }
}