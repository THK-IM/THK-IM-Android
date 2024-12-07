package com.thk.im.android.live.engine

import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioRenderProxy : AudioProcessing {

    // int sampleRateHz, int numChannels
    override fun initialize(p0: Int, p1: Int) {
//        LLog.d("LiveAudioProxy", "Render initialize $p0, $p1")
    }

    override fun reset(p0: Int) {
//        LLog.d("LiveAudioProxy", "Render reset( $p0")
    }

    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(p0: Int, p1: Int, byteBuffer: ByteBuffer) {
//        p2.clear()
    }
}