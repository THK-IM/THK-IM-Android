package com.thk.im.android.live.room

import android.util.Base64
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.live.IMLiveManager
import com.thk.im.android.live.Role
import com.thk.im.android.live.api.vo.PlayStreamReqVo
import com.thk.im.android.live.api.vo.PlayStreamResVo
import org.webrtc.MediaStreamTrack
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription

class RemoteParticipant(
    uId: Long,
    roomId: String,
    role: Int,
    private val subStreamKey: String,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uId, roomId, role) {

    private var streamKey: String? = null

    override fun initPeerConn() {
        super.initPeerConn()
        peerConnection?.let {
            if (audioEnable && role == Role.Broadcaster.value) {
                it.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
            }
            if (videoEnable && role == Role.Broadcaster.value) {
                it.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
            }
        }
        if (peerConnection == null) {
            onError("initPeerConn", Exception("create peer connection failed"))
        }
    }

    override fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        super.onLocalSdpSetSuccess(sdp)
        val offer = sdp.description
        val offerBase64 = String(
            Base64.encode(
                offer.toByteArray(Charsets.UTF_8),
                Base64.DEFAULT
            )
        )
        val reqVo = PlayStreamReqVo(
            roomId, IMLiveManager.shared().selfId, offerBase64, subStreamKey
        )
        val subscriber = object : BaseSubscriber<PlayStreamResVo>() {
            override fun onNext(t: PlayStreamResVo?) {
                t?.let {
                    val answer = String(
                        Base64.decode(
                            it.answerSdp.toByteArray(Charsets.UTF_8),
                            Base64.DEFAULT
                        )
                    )
                    streamKey = t.streamKey
                    val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    setRemoteSessionDescription(remoteSdp)
                }
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                t?.let {
                    this@RemoteParticipant.onError("playStream", Exception(it))
                }
            }
        }
        IMLiveManager.shared().liveApi
            .playStream(reqVo)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    override fun pushStreamKey(): String {
        return this.subStreamKey
    }

    override fun playStreamKey(): String? {
        return this.streamKey
    }
}