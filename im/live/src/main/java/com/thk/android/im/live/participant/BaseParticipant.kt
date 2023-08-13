package com.thk.android.im.live.participant

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.bean.NewStreamNotify
import com.thk.android.im.live.bean.NotifyBean
import com.thk.android.im.live.bean.RemoveStreamNotify
import com.thk.android.im.live.utils.LLog
import com.thk.android.im.live.utils.MediaConstraintsHelper
import com.thk.android.im.live.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RendererCommon
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import java.nio.charset.Charset

abstract class BaseParticipant(
    val uid: String,
    val roomId: String,
) : PeerConnection.Observer, DataChannel.Observer {

    private var dataChannelMap: HashMap<String, DataChannel> = HashMap()
    private val audioTracks = mutableListOf<AudioTrack>()
    private val videoTracks = mutableListOf<VideoTrack>()
    private var innerDataChannel: DataChannel? = null
    protected val compositeDisposable = CompositeDisposable()
    protected var peerConnection: PeerConnection? = null
    private var svr: SurfaceViewRenderer? = null
    private val handler = Handler(Looper.getMainLooper())

    open fun initPeerConn() {
        val configuration = PeerConnection.RTCConfiguration(emptyList())
        //必须设置PeerConnection.SdpSemantics.UNIFIED_PLAN
        configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        this.peerConnection = LiveManager.shared().getPCFactoryWrapper()
            .factory
            .createPeerConnection(configuration, this)
    }

    open fun startPeerConnection(peerConnection: PeerConnection) {
        if (this is LocalParticipant) {
            innerDataChannel = peerConnection.createDataChannel("", DataChannel.Init().apply {
                ordered = true
                maxRetransmits = 3
            })
            innerDataChannel?.registerObserver(this)
        }
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                LLog.d("${uid}, createOffer onCreateSuccess")
                p0?.let {
                    if (it.type == SessionDescription.Type.OFFER) {
                        onLocalSdpCreated(it)
                    }
                }
            }

            override fun onSetSuccess() {
                LLog.d("${uid}, createOffer onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uid}, createOffer onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uid}, createOffer onSetFailure $p0")
            }

        }, MediaConstraintsHelper.offerOrAnswerConstraint(this is RemoteParticipant))
    }

    fun setAudioEnable(enable: Boolean) {
        audioTracks.let {
            for (i in it) {
                i.setEnabled(enable)
            }
        }
    }

    fun setVideoEnable(enable: Boolean) {
        videoTracks.let {
            for (i in it) {
                i.setEnabled(enable)
            }
        }
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
                LLog.d("${uid}, setLocalDescription onCreateSuccess")
            }

            override fun onSetSuccess() {
                LLog.d("${uid}, setLocalDescription onSetSuccess")
                peerConnection?.let {
                    onLocalSdpSetSuccess(it.localDescription)
                }
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uid}, setLocalDescription onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uid}, setLocalDescription onSetFailure $p0")
            }

        }, sdp)
    }

    open fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        LLog.d("${uid}, onLocalSdpSetSuccess, ${sdp.description}")
    }

    fun setRemoteSessionDescription(sdp: SessionDescription) {
        LLog.d("${uid}, setRemoteSessionDescription, ${sdp.description}")
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                LLog.d("${uid}, setRemoteDescription onCreateSuccess")
            }

            override fun onSetSuccess() {
                LLog.d("${uid}, setRemoteDescription onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uid}, setRemoteDescription onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uid}, setRemoteDescription onSetFailure $p0")
            }
        }, sdp)
    }


    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        LLog.d("${uid}, onSignalingChange $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        LLog.d("${uid}, onIceConnectionChange $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        LLog.d("${uid}, onIceConnectionReceivingChange $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        LLog.d("${uid}, onIceGatheringChange $p0")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        LLog.d("${uid}, onIceCandidate $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        LLog.d("${uid}, onIceCandidatesRemoved $p0")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        LLog.d("${uid}, onConnectionChange ${newState.toString()}")
        peerConnection?.connectionState()?.let {
            when (it) {
                PeerConnection.PeerConnectionState.CONNECTING, PeerConnection.PeerConnectionState.NEW -> {
                    // 连接中
                }

                PeerConnection.PeerConnectionState.CONNECTED -> {
                    // 已经连接
                }

                else -> {
                    // 断开连接
                    onDisConnected()
                }
            }
        }
    }

    override fun onAddStream(p0: MediaStream?) {
        handler.post {
            LLog.d("${uid}, onAddStream")
            p0?.let {
                LLog.d("${uid}, onAddStream, ${it.audioTracks.size}, ${it.videoTracks.size}")
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
        LLog.d("${uid}, onRemoveStream")
        p0?.let {
            LLog.d("${uid}, onRemoveStream, ${it.audioTracks.size}, ${it.videoTracks.size}")
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
        LLog.d("${uid}, onDataChannel")
        p0?.let {
            it.registerObserver(this)
            it.label()?.let { label ->
                dataChannelMap[label] = it
            }
        }
    }

    override fun onRenegotiationNeeded() {
        LLog.d("${uid}, onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        super.onAddTrack(p0, p1)
        LLog.d("${uid}, onAddTrack")
        p0?.let {
            LLog.d("${uid}, onAddTrack, p0: $p0")
        }
        p1?.let {
            it.forEach { stream ->
                LLog.d("${uid}, onAddTrack, p1: ${stream.audioTracks.size}, ${stream.videoTracks.size}")
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver?) {
        super.onRemoveTrack(receiver)
//        LLog.d("${uid}, onRemoveTrack")
//        receiver?.track()?.let {
//            LLog.d("${uid}, onRemoveTrack: ${it.kind()}, $it")
//            if (it is AudioTrack) {
//                removeAudioTrack(it)
//            }
//            if (it is VideoTrack) {
//                removeVideoTrack(it)
//            }
//        }
    }

    override fun onBufferedAmountChange(p0: Long) {
        LLog.d("onBufferedAmountChange, p0: $p0")
    }

    override fun onStateChange() {
        LLog.d("onStateChange, connect status: ${peerConnection?.connectionState()}")
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        p0?.let {
            if (it.binary) {
                it.data?.let { data ->
                    LLog.d("onMessage: " + Utils.byteArray2HexString(data.array()))
                }
            } else {
                it.data?.let { data ->
                    val charset = Charset.forName("utf-8")
                    val message = charset.decode(data).toString()
                    LLog.d("onMessage: $message")
                    this.onNewMessage(message)
                }
            }
        }
    }

    private fun onNewMessage(message: String) {
        val notify = Gson().fromJson(message, NotifyBean::class.java)
        if (notify.type == "NewStream") {
            val newStream = Gson().fromJson(notify.message, NewStreamNotify::class.java)
            val participant =
                RemoteParticipant(newStream.uid, newStream.roomId, newStream.streamKey)
            handler.post {
                val currentRoom = LiveManager.shared().getRoom()
                currentRoom?.participantJoin(participant)
            }
        } else if (notify.type == "RemoveStream") {
            val removeStreamNotify = Gson().fromJson(notify.message, RemoveStreamNotify::class.java)
            handler.post {
                val currentRoom = LiveManager.shared().getRoom()
                currentRoom?.participantLeave(
                    removeStreamNotify.roomId,
                    removeStreamNotify.streamKey
                )
            }
        }
    }

    protected fun addVideoTrack(videoTrack: VideoTrack) {
        LLog.d("$uid, addVideoTrack")
        videoTracks.add(videoTrack)
        attach()
    }

    private fun removeVideoTrack(videoTrack: VideoTrack) {
        LLog.d("$uid, removeVideoTrack")
        videoTracks.remove(videoTrack)
        detach()
    }

    protected fun addAudioTrack(audioTrack: AudioTrack) {
        LLog.d("$uid, addAudioTrack")
        audioTracks.add(audioTrack)
    }

    private fun removeAudioTrack(audioTrack: AudioTrack) {
        LLog.d("$uid, removeAudioTrack")
        audioTracks.remove(audioTrack)
    }

    open fun attachViewRender(svr: SurfaceViewRenderer) {
        LLog.d("$uid, attachViewRender $svr")
        if (this.svr != null) {
            detach()
        }
        this.svr = svr
        attach()
    }

    private fun attach() {
        LLog.d("$uid, attach, ${videoTracks.size}, $svr")
        svr?.let {
            if (videoTracks.size > 0) {
                it.init(LiveManager.shared().getPCFactoryWrapper().eglCtx, null)
                it.setMirror(true)
                it.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                it.keepScreenOn = true
                it.setZOrderMediaOverlay(true)
                it.setEnableHardwareScaler(false)
                for (v in videoTracks) {
                    v.addSink(it)
                }
            }
        }

    }

    open fun detachViewRender() {
        LLog.d("$uid ,detachViewRender")
        detach()
    }

    private fun detach() {
        LLog.d("$uid , detach ${this.svr.toString()}")
        svr?.let {
            it.release()
            LLog.d("detach svr ${videoTracks.size}")
            for (v in videoTracks) {
                v.removeSink(it)
            }
            videoTracks.clear()
        }
        svr = null
    }

    open fun leave() {
        peerConnection?.dispose()
        peerConnection = null
    }

    open fun onDisConnected() {
        LLog.d("$uid, onDisConnected: ")
        detachViewRender()
        videoTracks.clear()
        audioTracks.clear()
        innerDataChannel?.let {
            it.unregisterObserver()
            it.dispose()
        }
        innerDataChannel = null
        for ((_, v) in dataChannelMap) {
            v.unregisterObserver()
            v.dispose()
        }
        dataChannelMap.clear()
        LiveManager.shared().getRoom()?.onParticipantLeave(this)
        handler.removeCallbacksAndMessages(null)
        compositeDisposable.clear()
    }

    abstract fun pushStreamKey(): String?

    abstract fun playStreamKey(): String?

}