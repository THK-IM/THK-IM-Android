package com.thk.im.android.live.room

import android.util.Base64
import com.google.gson.Gson
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.DataChannelMsg
import com.thk.im.android.live.LiveManager
import com.thk.im.android.live.Role
import com.thk.im.android.live.VolumeMsg
import com.thk.im.android.live.api.vo.MediaParams
import com.thk.im.android.live.api.vo.PublishStreamReqVo
import com.thk.im.android.live.api.vo.PublishStreamResVo
import com.thk.im.android.live.engine.LiveMediaConstraints
import com.thk.im.android.live.engine.LiveRTCEngine
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import java.nio.ByteBuffer

class LocalParticipant(
    uId: Long,
    roomId: String,
    role: Int,
    private val mediaParams: MediaParams,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uId, roomId, role) {

    private var pushStreamKey: String? = null
    private var videoSource: VideoSource? = null
    private var videoCapture: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var innerDataChannel: DataChannel? = null
    private var cameraName: String? = null

    override fun initPeerConn() {
        super.initPeerConn()
        if (peerConnection != null) {
            if (audioEnable && role == Role.Broadcaster.value) {
                val audioSource = LiveRTCEngine.shared().factory.createAudioSource(
                    LiveMediaConstraints.build(
                        enable3a = true, enableCpu = true, enableGainControl = true
                    )
                )
                // 创建AudioTrack，音频轨
                val audioTrack = LiveRTCEngine.shared().factory.createAudioTrack(
                    "audio/$roomId/$uId",
                    audioSource
                )
                peerConnection?.addTransceiver(
                    audioTrack,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                )

                addAudioTrack(audioTrack)

                peerConnection?.senders?.forEach { sender ->
                    if (sender.track()?.kind() == audioTrack.kind()) {
                        val parameters = sender.parameters
                        for (e in parameters.encodings) {
                            e.maxBitrateBps = mediaParams.audioMaxBitrate
                        }
                        sender.parameters = parameters
                    }
                }
            }

            if (videoEnable && role == Role.Broadcaster.value) {
                surfaceTextureHelper =
                    SurfaceTextureHelper.create(
                        "surface_texture_thread",
                        LiveRTCEngine.shared().eglBaseCtx
                    )
                videoSource = LiveRTCEngine.shared().factory.createVideoSource(false)
                val videoProcessor = LiveRTCEngine.shared().videoCaptureProxy()
                videoProcessor?.let { processor ->
                    videoSource?.setVideoProcessor(processor)
                }
                val videoTrack = LiveRTCEngine.shared().factory.createVideoTrack(
                    "video/$roomId/$uId",
                    videoSource
                )
                peerConnection?.addTransceiver(
                    videoTrack,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                )
                addVideoTrack(videoTrack)
                peerConnection?.senders?.forEach { sender ->
                    if (sender.track()?.kind() == videoTrack.kind()) {
                        val parameters = sender.parameters
                        for (e in parameters.encodings) {
                            e.maxBitrateBps = mediaParams.videoMaxBitrate
                        }
                        sender.parameters = parameters
                    }
                }
                startCaptureVideo()
            }
            innerDataChannel = peerConnection!!.createDataChannel("", DataChannel.Init().apply {
                ordered = true
                maxRetransmits = 3
            })
            innerDataChannel?.registerObserver(this)
        } else {
            onError("initPeerConn", Exception("peer connection create failed"))
        }
    }

    fun currentCamera(): Int {
        if (cameraName == null) {
            return 0
        }
        val enumerator =
            if (Camera2Enumerator.isSupported(LiveManager.shared().app)) Camera2Enumerator(
                LiveManager.shared().app
            ) else Camera1Enumerator()
        return if (enumerator.isFrontFacing(cameraName!!)) {
            2
        } else {
            1
        }
    }

    override fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        super.onLocalSdpSetSuccess(sdp)
        val offer = sdp.description
        val offerBase64 =
            String(Base64.encode(offer.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        val reqVo = PublishStreamReqVo(roomId, uId, offerBase64)
        val subscriber = object : BaseSubscriber<PublishStreamResVo>() {
            override fun onNext(t: PublishStreamResVo?) {
                t?.let {
                    val answer = String(
                        Base64.decode(
                            it.answerSdp.toByteArray(Charsets.UTF_8),
                            Base64.DEFAULT
                        )
                    )
                    pushStreamKey = t.streamKey
                    val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    setRemoteSessionDescription(remoteSdp)
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.let {
                    this@LocalParticipant.onError("publishStream", Exception(it))
                }
            }
        }
        RTCRoomManager.shared().liveApi.publishStream(reqVo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    private fun startCaptureVideo() {
        val app = LiveManager.shared().app ?: return
        var name = getFrontCameraName()
        if (name == null) {
            name = getBackCameraName()
        }
        if (name == null) return
        val enumerator =
            if (Camera2Enumerator.isSupported(app)) Camera2Enumerator(app) else Camera1Enumerator()
        this.videoCapture = enumerator.createCapturer(name, null)
        this.videoCapture?.initialize(surfaceTextureHelper, app, videoSource!!.capturerObserver)
        this.videoCapture?.startCapture(
            mediaParams.videoWidth,
            mediaParams.videoHeight,
            mediaParams.videoFps
        )
        cameraName = name
    }

    private fun getFrontCameraName(): String? {
        val context = LiveManager.shared().app ?: return null
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        var cameraName: String? = null
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                cameraName = name
                break
            }
        }
        return cameraName
    }

    private fun getBackCameraName(): String? {
        val context = LiveManager.shared().app ?: return null
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        var cameraName: String? = null
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                cameraName = name
                break
            }
        }
        return cameraName
    }

    fun switchCamera(): Boolean {
        val context = LiveManager.shared().app ?: return false
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        var name = getFrontCameraName()
        if (cameraName == null) {
            if (name == null) {
                name = getBackCameraName()
            }
        } else {
            name = if (enumerator.isFrontFacing(cameraName)) {
                getBackCameraName()
            } else {
                getFrontCameraName()
            }
        }

        if (name == null) return false
        videoCapture?.switchCamera(null, name)
        cameraName = name
        return true
    }

    override fun onDisConnected() {
        super.onDisConnected()
        innerDataChannel?.let {
            it.unregisterObserver()
            it.dispose()
        }
        innerDataChannel = null
        LiveRTCEngine.shared().clearVideoProxy()
        videoSource?.dispose()
        videoCapture?.dispose()
        surfaceTextureHelper?.dispose()
        videoCapture = null
        videoSource = null
        surfaceTextureHelper = null
    }

    override fun pushStreamKey(): String? {
        return this.pushStreamKey
    }

    override fun playStreamKey(): String? {
        return null
    }

    fun sendMyVolume(volume: Double): Boolean {
        if (role == Role.Broadcaster.value) {
            val volumeMsg = VolumeMsg(this.uId, volume)
            val text = Gson().toJson(volumeMsg)
            return sendMessage(0, text)
        }
        return false
    }

    fun sendMessage(type: Int, text: String): Boolean {
        return if (innerDataChannel == null) {
            false
        } else {
            val msg = DataChannelMsg(type, text)
            val msgStr = Gson().toJson(msg)
            val buffer = DataChannel.Buffer(ByteBuffer.wrap(msgStr.toByteArray()), false)
            innerDataChannel!!.send(buffer)
        }
    }

    fun sendBytes(ba: ByteArray): Boolean {
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(ba), true)
        return innerDataChannel?.send(buffer) ?: false
    }

    fun sendByteBuffer(bb: ByteBuffer): Boolean {
        val buffer = DataChannel.Buffer(bb, true)
        return innerDataChannel?.send(buffer) ?: false
    }
}