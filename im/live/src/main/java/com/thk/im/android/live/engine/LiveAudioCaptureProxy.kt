package com.thk.im.android.live.engine

import com.thk.im.android.core.base.LLog
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioCaptureProxy : AudioProcessing {

    private var lastCal: Long = 0

    // int sampleRateHz, int numChannels
    override fun initialize(p0: Int, p1: Int) {
        LLog.d("LiveAudioProxy", "Capture initialize $p0, $p1")
    }

    override fun reset(p0: Int) {
        LLog.d("LiveAudioProxy", "Capture reset $p0")
    }

    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(p0: Int, p1: Int, p2: ByteBuffer) {
        val current = System.currentTimeMillis()
//        if (current - lastCal > 500) {
//            val length = min(p1, 256)
//            val data = ByteArray(length)
//            p2.get(data, 0, length)
//            LiveRTCEngine.shared().captureOriginAudio(data)
//            lastCal = current
//        }
    }
}