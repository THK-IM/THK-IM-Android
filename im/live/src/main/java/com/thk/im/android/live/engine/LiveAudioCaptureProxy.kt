package com.thk.im.android.live.engine

import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioCaptureProxy: AudioProcessing {
    override fun initialize(p0: Int, p1: Int) {
    }

    override fun reset(p0: Int) {
    }

    override fun process(p0: Int, p1: Int, p2: ByteBuffer?) {
    }
}