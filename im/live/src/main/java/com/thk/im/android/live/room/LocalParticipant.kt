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
                LiveManager.shared().app?.let { app ->
                    videoCapture = createVideoCapture()
                    videoCapture?.let {
                        surfaceTextureHelper =
                            SurfaceTextureHelper.create(
                                "surface_texture_thread",
                                LiveRTCEngine.shared().eglBaseCtx
                            )
                        videoSource =
                            LiveRTCEngine.shared().factory.createVideoSource(it.isScreencast)
                        val videoProcessor = LiveRTCEngine.shared().videoCaptureProxy()
                        videoProcessor?.let { processor ->
                            videoSource?.setVideoProcessor(processor)
                        }
                        val videoTrack =
                            LiveRTCEngine.shared().factory.createVideoTrack(
                                "video/$roomId/$uId",
                                videoSource
                            )
                        peerConnection?.addTransceiver(
                            videoTrack,
                            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                        )
                        it.initialize(surfaceTextureHelper, app, videoSource!!.capturerObserver)

                        it.startCapture(
                            mediaParams.videoWidth,
                            mediaParams.videoHeight,
                            mediaParams.videoFps
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
                    }
                }
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

    private fun createVideoCapture(): CameraVideoCapturer? {
        val context = LiveManager.shared().app ?: return null
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        cameraName = getBackCameraName()
        if (cameraName != null) {
            return enumerator.createCapturer(cameraName!!, null)
        }
        return null
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

    fun switchCamera() {
        if (cameraName == null) return
        val context = LiveManager.shared().app ?: return
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        cameraName = if (enumerator.isFrontFacing(cameraName)) {
            getBackCameraName()
        } else {
            getFrontCameraName()
        }
        videoCapture?.switchCamera(null, cameraName)
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

    fun sendMyVolume(volume: Double) {
        if (role == Role.Broadcaster.value) {
            val volumeMsg = VolumeMsg(this.uId, volume)
            val text = Gson().toJson(volumeMsg)
            sendMessage(0, text)
        }
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