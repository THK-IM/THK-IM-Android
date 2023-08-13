package com.thk.android.im.live.participant

import android.content.Context
import android.util.Base64
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.api.ApiManager
import com.thk.android.im.live.api.BaseSubscriber
import com.thk.android.im.live.api.RtcApi
import com.thk.android.im.live.api.RxTransform
import com.thk.android.im.live.bean.PublishReqBean
import com.thk.android.im.live.bean.PublishResBean
import com.thk.android.im.live.room.Role
import com.thk.android.im.live.utils.LLog
import com.thk.android.im.live.utils.MediaConstraintsHelper
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import java.nio.charset.Charset

class LocalParticipant(
    uid: String,
    roomId: String,
    private var role: Role,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uid, roomId) {

    private var pushStreamKey: String? = null
    private var videoSource: VideoSource? = null
    private var videoCapture: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

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
                    videoCapture = createVideoCapture(app)
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
                        it.startCapture(320, 480, 10)
                        addVideoTrack(videoTrack)
                    }
                }
            }
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

    private fun createVideoCapture(context: Context): CameraVideoCapturer? {
        //优先使用Camera2
        val enumerator =
            if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator()
        val deviceNames = enumerator.deviceNames
        //前置
        for (name in deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        //后置
        for (name in deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        return null
    }

    override fun onDisConnected() {
        super.onDisConnected()
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

    fun getRole(): Role {
        return this.role
    }
}