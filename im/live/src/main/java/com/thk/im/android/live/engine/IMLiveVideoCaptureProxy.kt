package com.thk.im.android.live.engine

import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink

class IMLiveVideoCaptureProxy : VideoProcessor {
    private var videoSink: VideoSink? = null
    override fun onCapturerStarted(p0: Boolean) {
    }

    override fun onCapturerStopped() {
    }

    override fun onFrameCaptured(p0: VideoFrame?) {
        p0?.let {
            videoSink?.onFrame(it)
        }
    }

    override fun setSink(p0: VideoSink?) {
        videoSink = p0
    }
}