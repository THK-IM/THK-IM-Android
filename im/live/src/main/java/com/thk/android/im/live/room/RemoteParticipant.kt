package com.thk.android.im.live.room

import android.util.Base64
import com.thk.android.im.live.api.ApiManager
import com.thk.android.im.live.api.RtcApi
import com.thk.android.im.live.base.BaseSubscriber
import com.thk.android.im.live.base.RxTransform
import com.thk.android.im.live.bean.PlayReqBean
import com.thk.android.im.live.bean.PlayResBean
import org.webrtc.MediaStreamTrack
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import java.nio.charset.Charset
class RemoteParticipant(
    uid: String,
    roomId: String,
    role: Role,
    private val subStreamKey: String,
    private val audioEnable: Boolean,
    private val videoEnable: Boolean
) : BaseParticipant(uid, roomId, role) {

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
        val bean = PlayReqBean(roomId, uid, offerBase64, streamKey = subStreamKey)
        val subscriber = object : BaseSubscriber<PlayResBean>() {
            override fun onNext(t: PlayResBean?) {
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
        ApiManager.getApi(RtcApi::class.java)
            .requestPlay(bean)
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