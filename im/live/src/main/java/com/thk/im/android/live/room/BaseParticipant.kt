package com.thk.im.android.live.room

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thk.im.android.core.base.LLog
import com.thk.im.android.live.DataChannelMsg
import com.thk.im.android.live.NewStreamNotify
import com.thk.im.android.live.NotifyBean
import com.thk.im.android.live.NotifyType
import com.thk.im.android.live.RemoveStreamNotify
import com.thk.im.android.live.engine.LiveMediaConstraints
import com.thk.im.android.live.engine.LiveRTCEngine
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import java.nio.ByteBuffer
import java.nio.charset.Charset

abstract class BaseParticipant(
    val uId: Long,
    val roomId: String,
    val role: Int,
) : PeerConnection.Observer, DataChannel.Observer {

    private var dataChannelMap: HashMap<String, DataChannel> = HashMap()
    private val audioTracks = mutableListOf<AudioTrack>()
    private val videoTracks = mutableListOf<VideoTrack>()
    protected val compositeDisposable = CompositeDisposable()
    protected var peerConnection: PeerConnection? = null
    private val handler = Handler(Looper.getMainLooper())
    private var audioMuted: Boolean = false
    private var videoMuted: Boolean = false
    private var svr: SurfaceViewRenderer? = null

    open fun initPeerConn() {
        val configuration = PeerConnection.RTCConfiguration(emptyList())
        //必须设置PeerConnection.SdpSemantics.UNIFIED_PLAN
        configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        this.peerConnection = LiveRTCEngine.shared()
            .factory.createPeerConnection(configuration, this)
    }

    open fun startPeerConnection() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                LLog.d("RTCRoom", "${this.javaClass} $uId  createOffer onCreateSuccess")
                p0?.let {
                    if (it.type == SessionDescription.Type.OFFER) {
                        val stereoSdp =
                            it.description.replace("useinbandfec=1", "useinbandfec=1;stereo=1")
                        val newSdp = SessionDescription(it.type, stereoSdp)
                        onLocalSdpCreated(newSdp)
                    }
                }
            }

            override fun onSetSuccess() {
                LLog.d("RTCRoom", "${this.javaClass} $uId createOffer onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                LLog.d("RTCRoom", "${this.javaClass} $uId  createOffer onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.d("RTCRoom", "${this.javaClass} $uId  createOffer onSetFailure $p0")
            }

        }, LiveMediaConstraints.offerOrAnswerConstraint(this is RemoteParticipant, true))
    }

    fun setAudioMuted(muted: Boolean) {
        audioTracks.let {
            for (i in it) {
                i.setEnabled(!muted)
            }
        }
        audioMuted = muted
    }

    fun getAudioMuted(): Boolean {
        return audioMuted
    }

    fun setVideoMuted(muted: Boolean) {
        videoTracks.let {
            for (i in it) {
                i.setEnabled(!muted)
            }
        }
        videoMuted = muted
    }

    fun getVideoMuted(): Boolean {
        return videoMuted
    }

    fun setVolume(volume: Double) {
        audioTracks.let {
            for (i in it) {
                i.setVolume(volume)
            }
        }
    }

    open fun onLocalSdpCreated(sdp: SessionDescription) {
        peerConnection?.setLocalDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onSetSuccess() {
                peerConnection?.let {
                    onLocalSdpSetSuccess(it.localDescription)
                }
            }

            override fun onCreateFailure(p0: String?) {
                onError("setLocalDescription", Exception("onCreateFailure: $p0"))
            }

            override fun onSetFailure(p0: String?) {
                onError("setLocalDescription", Exception("onSetFailure: $p0"))
            }

        }, sdp)
    }

    open fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        LLog.d("${uId}, onLocalSdpSetSuccess, ${sdp.description}")
    }

    fun setRemoteSessionDescription(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(p0: String?) {
                LLog.d("RTCRoom", "${this.javaClass} $uId onCreateFailure $p0")
                onError("setRemoteDescription", Exception("onCreateFailure: $p0"))
            }

            override fun onSetFailure(p0: String?) {
                LLog.d("RTCRoom", "${this.javaClass} $uId onSetFailure $p0")
                onError("setRemoteDescription", Exception("onSetFailure: $p0"))
            }
        }, sdp)
    }


    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onSignalingChange $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onIceConnectionChange $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onIceConnectionReceivingChange $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onIceGatheringChange $p0")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onIceCandidate $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onIceCandidatesRemoved $p0")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        LLog.d("RTCRoom", "${this.javaClass} $uId onConnectionChange")
        peerConnection?.connectionState()?.let {
            if (it == PeerConnection.PeerConnectionState.CLOSED ||
                it == PeerConnection.PeerConnectionState.DISCONNECTED ||
                it == PeerConnection.PeerConnectionState.FAILED
            ) {
                // 断开连接
                onDisConnected()
            }
            onConnectStatusChange(it.ordinal)
        }
    }

    override fun onAddStream(p0: MediaStream?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onAddStream")
        p0?.let {
            handler.post {
                if (this is RemoteParticipant) {
                    if (it.videoTracks != null) {
                        for (v in it.videoTracks) {
                            this.addVideoTrack(v)
                        }
                    }
                    if (it.audioTracks != null) {
                        for (v in it.audioTracks) {
                            this.addAudioTrack(v)
                        }
                    }
                }
            }
        }
    }

    override fun onRemoveStream(p0: MediaStream?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onRemoveStream")
        p0?.let {
            if (this is RemoteParticipant) {
                if (it.videoTracks != null) {
                    handler.post {
                        detach()
                    }
                }
                if (it.audioTracks != null) {
                    for (audio in it.audioTracks) {
                        audio.dispose()
                        audioTracks.remove(audio)
                    }
                }
            }
        }
    }

    override fun onDataChannel(p0: DataChannel?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId onDataChannel")
        p0?.let {
            it.registerObserver(this)
            it.label()?.let { label ->
                dataChannelMap[label] = it
            }
        }
    }

    override fun onRenegotiationNeeded() {
        LLog.d("RTCRoom", "${this.javaClass} $uId onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        super.onAddTrack(p0, p1)
        LLog.d("RTCRoom", "${this.javaClass} $uId onAddTrack")
    }

    override fun onRemoveTrack(receiver: RtpReceiver?) {
        super.onRemoveTrack(receiver)
        LLog.d("RTCRoom", "${this.javaClass} $uId onRemoveTrack")
    }

    override fun onBufferedAmountChange(p0: Long) {
    }

    override fun onStateChange() {
        LLog.d(
            "RTCRoom",
            "${this.javaClass} $uId onStateChange, connect status: ${peerConnection?.connectionState()}"
        )
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, onMessage")
        p0?.let {
            if (it.binary) {
                it.data?.let { data ->
                    this.onNewBufferMessage(data)
                }
            } else {
                it.data?.let { data ->
                    val charset = Charset.forName("utf-8")
                    val message = charset.decode(data).toString()
                    this.onNewMessage(message)
                }
            }
        }
    }

    open fun onNewBufferMessage(bb: ByteBuffer) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, onNewBufferMessage")
        RTCRoomManager.shared().getRoomById(roomId)?.let { room ->
            handler.post {
                room.receivedDcMsg(bb)
            }
        }
    }

    open fun onNewMessage(message: String) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, onNewMessage")
        val notify = Gson().fromJson(message, NotifyBean::class.java)
        when (notify.type) {
            NotifyType.NewStream.value -> {
                val newStream = Gson().fromJson(notify.message, NewStreamNotify::class.java)
                RTCRoomManager.shared().getRoomById(roomId)?.let {
                    val remoteParticipant = RemoteParticipant(
                        newStream.uId,
                        newStream.roomId,
                        newStream.role,
                        newStream.streamKey,
                        it.audioEnable(),
                        it.videoEnable()
                    )
                    handler.post {
                        it.participantJoin(remoteParticipant)
                    }
                }
            }

            NotifyType.RemoveStream.value -> {
                val removeStreamNotify =
                    Gson().fromJson(notify.message, RemoveStreamNotify::class.java)
                RTCRoomManager.shared().getRoomById(roomId)?.let {
                    handler.post {
                        it.participantLeave(
                            removeStreamNotify.roomId,
                            removeStreamNotify.streamKey
                        )
                    }
                }
            }

            NotifyType.DataChannelMsg.value -> {
                val dataChannelMsg = Gson().fromJson(notify.message, DataChannelMsg::class.java)
                RTCRoomManager.shared().getRoomById(roomId)?.let {
                    handler.post {
                        it.receivedDcMsg(dataChannelMsg.type, dataChannelMsg.text)
                    }
                }
            }
        }
    }

    open fun onConnectStatusChange(status: Int) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, onConnectStatusChange")
        val room = RTCRoomManager.shared().getRoomById(roomId) ?: return
        if (room.id != this.roomId) {
            return
        }
        room.onConnectStatusChange(uId, status)
    }

    open fun onError(function: String, exception: Exception) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, onError ${exception.message}")
        val room = RTCRoomManager.shared().getRoomById(roomId) ?: return
        room.callback?.onError(function, exception)
    }

    protected fun addVideoTrack(videoTrack: VideoTrack) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, addVideoTrack")
        videoTracks.add(videoTrack)
        attach()
    }

    private fun removeVideoTrack(videoTrack: VideoTrack) {
        LLog.d("RTCRoom", "${this.javaClass} $uId, addVideoTrack")
        videoTracks.remove(videoTrack)
        detach()
    }

    protected fun addAudioTrack(audioTrack: AudioTrack) {
        audioTracks.add(audioTrack)
    }

    private fun removeAudioTrack(audioTrack: AudioTrack) {
        audioTracks.remove(audioTrack)
    }

    open fun attachViewRender(svr: SurfaceViewRenderer) {
        if (this.svr != null) {
            detach()
        }
        this.svr = svr
        attach()
    }

    private fun attach() {
        svr?.let {
            if (videoTracks.size > 0) {
                it.init(LiveRTCEngine.shared().eglBaseCtx, null)
                for (v in videoTracks) {
                    v.addSink(it)
                }
            }
        }

    }

    open fun detachViewRender() {
        detach()
    }

    private fun detach() {
        svr?.let {
            it.release()
            for (v in videoTracks) {
                v.removeSink(it)
            }
        }
        svr = null
    }

    open fun leave() {
        peerConnection?.dispose()
        peerConnection = null
    }

    open fun onDisConnected() {
        detachViewRender()
        videoTracks.clear()
        audioTracks.clear()
        for ((_, v) in dataChannelMap) {
            v.unregisterObserver()
            v.dispose()
        }
        dataChannelMap.clear()
        RTCRoomManager.shared().getRoomById(roomId)?.onParticipantLeave(this)
        handler.removeCallbacksAndMessages(null)
        compositeDisposable.clear()
    }

    abstract fun pushStreamKey(): String?

    abstract fun playStreamKey(): String?

}