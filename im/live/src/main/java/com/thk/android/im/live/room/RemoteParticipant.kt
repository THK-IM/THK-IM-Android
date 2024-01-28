package com.thk.android.im.live.room

import android.util.Base64
import com.thk.android.im.live.LiveApi
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.Role
import com.thk.android.im.live.api.RtcApi
import com.thk.android.im.live.vo.PlayStreamReqVo
import com.thk.android.im.live.vo.PlayStreamResVo
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import org.webrtc.MediaStreamTrack
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import java.nio.charset.Charset
class RemoteParticipant(
    uId: Long,
    roomId: String,
    role: Role,
    private val subStreamKey: String,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uId, roomId, role) {

    private var streamKey: String? = null

    override fun initPeerConn() {
        super.initPeerConn()
        peerConnection?.let {
            if (audioEnable && role == Role.Broadcaster) {
                it.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
            }
            if (videoEnable && role == Role.Broadcaster) {
                it.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
            }
            startPeerConnection(it)
        }
    }

    override fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        super.onLocalSdpSetSuccess(sdp)
        val offer = sdp.description
        val offerBase64 =
            String(Base64.encode(offer.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT))
        val reqVo = PlayStreamReqVo(roomId, uId, offerBase64, streamKey = subStreamKey)
        val subscriber = object : BaseSubscriber<PlayStreamResVo>() {
            override fun onNext(t: PlayStreamResVo?) {
                t?.let {
                    val answer = String(
                        Base64.decode(
                            it.answerSdp.toByteArray(Charset.forName("UTF-8")),
                            Base64.DEFAULT
                        )
                    )
                    streamKey = t.streamKey
                    val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    setRemoteSessionDescription(remoteSdp)
                }
            }
        }
        LiveManager.shared().liveApi
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