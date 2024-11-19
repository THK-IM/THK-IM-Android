package com.thk.im.android.live.engine

import com.thk.im.android.core.base.LLog
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioCaptureProxy : AudioProcessing {

    private var lastCal: Long = 0

    override fun initialize(p0: Int, p1: Int) {
        // p0是采样率 p1 声道
        LLog.d("LiveAudioProxy", "Capture initialize $p0, $p1")
    }

    override fun reset(p0: Int) {
        LLog.d("LiveAudioProxy", "Capture reset $p0")
    }

    override fun process(p0: Int, p1: Int, p2: ByteBuffer?) {
        val current = System.currentTimeMillis()
        if (current - lastCal > 500) {
            LLog.d("LiveAudioProxy", "process $p0, $p1")
            lastCal = current
        }
    }
}