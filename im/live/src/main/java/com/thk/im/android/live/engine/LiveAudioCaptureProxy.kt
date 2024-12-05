package com.thk.im.android.live.engine

import com.thk.im.android.core.base.LLog
import org.webrtc.ExternalAudioProcessingFactory.AudioProcessing
import java.nio.ByteBuffer

open class LiveAudioCaptureProxy : AudioProcessing {

    // int sampleRateHz, int numChannels
    override fun initialize(p0: Int, p1: Int) {
        LLog.d("LiveAudioProxy", "Capture initialize $p0, $p1")
        LiveRTCEngine.shared().mediaPlayer?.updateRTCParams(p1, p0)
    }

    override fun reset(p0: Int) {
        LLog.d("LiveAudioProxy", "Capture reset $p0")
    }

    // int numBands, int numFrames, ByteBuffer buffer
    override fun process(p0: Int, p1: Int, p2: ByteBuffer) {
        val mediaPlayer = LiveRTCEngine.shared().mediaPlayer ?: return
        val mediaPCMData = mediaPlayer.fetchRTCPCM(p0 * p1) ?: return
        LLog.d("LiveAudioProxy", "process $p0 $p1, intercepet")
        val pcmData = ByteArray(p0 * p1)
        p2.get(pcmData)
        val mixPcm = PCMTransUtil.averageMix(arrayOf(pcmData, mediaPCMData))
        p2.clear()
        p2.rewind()
        p2.put(mixPcm)
    }
}