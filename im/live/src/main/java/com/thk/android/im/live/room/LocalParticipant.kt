package com.thk.android.im.live.room

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.api.ApiManager
import com.thk.android.im.live.api.RtcApi
import com.thk.android.im.live.bean.PublishReqBean
import com.thk.android.im.live.bean.PublishResBean
import com.thk.android.im.live.utils.MediaConstraintsHelper
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import java.nio.ByteBuffer
import java.nio.charset.Charset
class LocalParticipant(
    uid: String,
    roomId: String,
    role: Role,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uid, roomId, role) {

    private var pushStreamKey: String? = null
    private var videoSource: VideoSource? = null
    private var videoCapture: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var innerDataChannel: DataChannel? = null
    private var cameraName: String? = null

    override fun initPeerConn() {
        super.initPeerConn()
        if (peerConnection != null) {
            val pcFactoryWrapper = LiveManager.shared().getPCFactoryWrapper()
            if (audioEnable && role == Role.Broadcaster) {
                val audioSource = pcFactoryWrapper.factory.createAudioSource(
                    MediaConstraintsHelper.build(
                        enable3a = true, enableCpu = true, enableGainControl = true
                    )
                )
                // 创建AudioTrack，音频轨
                val audioTrack = pcFactoryWrapper.factory.createAudioTrack(
                    "audio/$roomId/$uid",
                    audioSource
                )
                peerConnection?.addTransceiver(
                    audioTrack,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                )

                addAudioTrack(audioTrack)
            }

            if (videoEnable && role == Role.Broadcaster) {
                LiveManager.shared().app?.let { app ->
                    videoCapture = createVideoCapture()
                    videoCapture?.let {
                        surfaceTextureHelper =
                            SurfaceTextureHelper.create(
                                "surface_texture_thread",
                                pcFactoryWrapper.eglCtx
                            )
                        videoSource =
                            pcFactoryWrapper.factory.createVideoSource(it.isScreencast)
                        val videoTrack =
                            pcFactoryWrapper.factory.createVideoTrack(
                                "video/$roomId/$uid",
                                videoSource
                            )
                        peerConnection?.addTransceiver(
                            videoTrack,
                            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                        )
                        it.initialize(surfaceTextureHelper, app, videoSource!!.capturerObserver)
                        it.startCapture(160, 240, 5)
                        addVideoTrack(videoTrack)
                    }
                }
            }
            innerDataChannel = peerConnection!!.createDataChannel("", DataChannel.Init().apply {
                ordered = true
                maxRetransmits = 3
            })
            innerDataChannel?.registerObserver(this)
            startPeerConnection(peerConnection!!)
        } else {
            LLog.e("peerConnection create failed")
        }
    }

    override fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        super.onLocalSdpSetSuccess(sdp)
        val offer = sdp.description
        val offerBase64 =
            String(Base64.encode(offer.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT))
        val bean = PublishReqBean(roomId, uid, offerBase64)
        val subscriber = object : BaseSubscriber<PublishResBean>() {
            override fun onNext(t: PublishResBean?) {
                t?.let {
                    val answer = String(
                        Base64.decode(
                            it.answerSdp.toByteArray(Charset.forName("UTF-8")),
                            Base64.DEFAULT
                        )
                    )
                    pushStreamKey = t.streamKey
                    LLog.d("remote sdp: $answer")
                    val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    setRemoteSessionDescription(remoteSdp)
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                LLog.e("remote sdp error: ${t?.message}")
            }
        }
        ApiManager.getApi(RtcApi::class.java)
            .requestPublish(bean)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    private fun createVideoCapture(): CameraVideoCapturer? {
        val context = LiveManager.shared().app ?: return null
        //优先使用Camera2
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                cameraName = name
                return enumerator.createCapturer(name, null)
            }
        }
        //后置
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                cameraName = name
                return enumerator.createCapturer(name, null)
            }
        }
        return null
    }

    private fun getFrontCameraName(): String? {
        val context = LiveManager.shared().app ?: return null
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return name
            }
        }
        return null
    }

    private fun getBackCameraName(): String? {
        val context = LiveManager.shared().app ?: return null
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return name
            }
        }
        return null
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


    fun sendMessage(text: String): Boolean {
        return if (innerDataChannel == null) {
            false
        } else {
            val msg = DataChannelMsg(uid, text)
            val msgStr = Gson().toJson(msg)
            val buffer = DataChannel.Buffer(ByteBuffer.wrap(msgStr.toByteArray()), false)
            innerDataChannel!!.send(buffer)
        }
    }

    fun sendBytes(ba: ByteArray): Boolean {
        return if (innerDataChannel == null) {
            false
        } else {
            val buffer = DataChannel.Buffer(ByteBuffer.wrap(ba), true)
            innerDataChannel!!.send(buffer)
        }
    }

    fun sendByteBuffer(bb: ByteBuffer): Boolean {
        return if (innerDataChannel == null) {
            false
        } else {
            val buffer = DataChannel.Buffer(bb, true)
            innerDataChannel!!.send(buffer)
        }
    }
}