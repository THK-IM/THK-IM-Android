package com.thk.im.android.live.engine

import com.thk.im.android.core.base.LLog
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer
import java.nio.ByteOrder


open class LiveAudioCaptureProxy : AudioProcessing {

    // int sampleRateHz, int numChannels
    override fun initialize(p0: Int, p1: Int) {
//        LLog.d("LiveAudioProxy", "Capture initialize $p0, $p1")
//        LiveRTCEngine.shared().mediaPlayer?.updateRTCParams(p1, p0)
    }

    override fun reset(p0: Int) {
//        LLog.d("LiveAudioProxy", "Capture reset $p0")
    }


    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(numBands: Int, numFrames: Int, byteBuffer: ByteBuffer) {
//        val mediaPlayer = LiveRTCEngine.shared().mediaPlayer ?: return
//        val mediaPCMData = mediaPlayer.fetchRTCPCM(byteBuffer.limit()) ?: return
//        byteBuffer.clear()
//        byteBuffer.put(mediaPCMData)
//        val originData = ByteArray(numFrames)
//        for (i in originData.indices) {
//
//        }
//        byteBuffer.clear()
//        byteBuffer.put(originData)
    }
}