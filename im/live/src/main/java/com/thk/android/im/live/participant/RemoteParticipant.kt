package com.thk.android.im.live.participant

import android.util.Base64
import com.thk.android.im.live.api.ApiManager
import com.thk.android.im.live.api.BaseSubscriber
import com.thk.android.im.live.api.RtcApi
import com.thk.android.im.live.api.RxTransform
import com.thk.android.im.live.bean.PlayReqBean
import com.thk.android.im.live.bean.PlayResBean
import org.webrtc.MediaStreamTrack
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import java.nio.charset.Charset

class RemoteParticipant(
    uid: String,
    roomId: String,
    private val pushStreamKey: String,
) : BaseParticipant(uid, roomId) {

    private var playStreamKey: String? = null

    override fun initPeerConn() {
        super.initPeerConn()
        peerConnection?.let {
            it.addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
            it.addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
            startPeerConnection(it)
        }
    }

    override fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        super.onLocalSdpSetSuccess(sdp)
        val offer = sdp.description
        val offerBase64 =
            String(Base64.encode(offer.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT))
        val bean = PlayReqBean(roomId, uid, offerBase64, streamKey = pushStreamKey)
        val subscriber = object : BaseSubscriber<PlayResBean>() {
            override fun onNext(t: PlayResBean?) {
                t?.let {
                    val answer = String(
                        Base64.decode(
                            it.answerSdp.toByteArray(Charset.forName("UTF-8")),
                            Base64.DEFAULT
                        )
                    )
                    playStreamKey = t.streamKey
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
        return this.pushStreamKey
    }

    override fun playStreamKey(): String? {
        return this.playStreamKey
    }
}