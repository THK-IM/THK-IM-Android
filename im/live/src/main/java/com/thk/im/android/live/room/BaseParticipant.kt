package com.thk.im.android.live.room

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thk.im.android.live.DataChannelMsg
import com.thk.im.android.live.IMLiveManager
import com.thk.im.android.live.Mode
import com.thk.im.android.live.NewStreamNotify
import com.thk.im.android.live.NotifyBean
import com.thk.im.android.live.NotifyType
import com.thk.im.android.live.RemoveStreamNotify
import com.thk.im.android.live.Role
import com.thk.im.android.live.utils.MediaConstraintsHelper
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.StringUtils
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
    val role: Role,
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
        this.peerConnection = IMLiveManager.shared().getPCFactoryWrapper()
            .factory
            .createPeerConnection(configuration, this)
    }

    open fun startPeerConnection() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                LLog.d("${uId}, createOffer onCreateSuccess")
                p0?.let {
                    if (it.type == SessionDescription.Type.OFFER) {
                        onLocalSdpCreated(it)
                    }
                }
            }

            override fun onSetSuccess() {
                LLog.d("${uId}, createOffer onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uId}, createOffer onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uId}, createOffer onSetFailure $p0")
            }

        }, MediaConstraintsHelper.offerOrAnswerConstraint(this is RemoteParticipant))
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
                LLog.d("${uId}, setLocalDescription onCreateSuccess")
            }

            override fun onSetSuccess() {
                LLog.d("${uId}, setLocalDescription onSetSuccess")
                peerConnection?.let {
                    onLocalSdpSetSuccess(it.localDescription)
                }
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uId}, setLocalDescription onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uId}, setLocalDescription onSetFailure $p0")
            }

        }, sdp)
    }

    open fun onLocalSdpSetSuccess(sdp: SessionDescription) {
        LLog.d("${uId}, onLocalSdpSetSuccess, ${sdp.description}")
    }

    fun setRemoteSessionDescription(sdp: SessionDescription) {
        LLog.d("${uId}, setRemoteSessionDescription, ${sdp.description}")
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                LLog.d("${uId}, setRemoteDescription onCreateSuccess")
            }

            override fun onSetSuccess() {
                LLog.d("${uId}, setRemoteDescription onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                LLog.e("${uId}, setRemoteDescription onCreateFailure $p0")
            }

            override fun onSetFailure(p0: String?) {
                LLog.e("${uId}, setRemoteDescription onSetFailure $p0")
            }
        }, sdp)
    }


    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        LLog.d("${uId}, onSignalingChange $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        LLog.d("${uId}, onIceConnectionChange $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        LLog.d("${uId}, onIceConnectionReceivingChange $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        LLog.d("${uId}, onIceGatheringChange $p0")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        LLog.d("${uId}, onIceCandidate $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        LLog.d("${uId}, onIceCandidatesRemoved $p0")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        LLog.d("${uId}, onConnectionChange ${newState.toString()}")
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
        LLog.d("${uId}, onAddStream")
        p0?.let {
            LLog.d("${uId}, onAddStream, ${it.audioTracks.size}, ${it.videoTracks.size}")
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
        LLog.d("${uId}, onRemoveStream")
        p0?.let {
            LLog.d("${uId}, onRemoveStream, ${it.audioTracks.size}, ${it.videoTracks.size}")
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
        LLog.d("${uId}, onDataChannel")
        p0?.let {
            it.registerObserver(this)
            it.label()?.let { label ->
                dataChannelMap[label] = it
            }
        }
    }

    override fun onRenegotiationNeeded() {
        LLog.d("${uId}, onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        super.onAddTrack(p0, p1)
        LLog.d("${uId}, onAddTrack")
    }

    override fun onRemoveTrack(receiver: RtpReceiver?) {
        super.onRemoveTrack(receiver)
        LLog.d("${uId}, onRemoveTrack")
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
                    LLog.d("onMessage: " + StringUtils.byteArray2HexString(data.array()))
                    this.onNewBufferMessage(it.data)
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

    open fun onNewBufferMessage(bb: ByteBuffer) {
        IMLiveManager.shared().getRoom()?.let {
            handler.post {
                it.receivedDcMsg(bb)
            }
        }
    }

    open fun onNewMessage(message: String) {
        LLog.d("$uId, onNewMessage: $message")
        val notify = Gson().fromJson(message, NotifyBean::class.java)
        when (notify.type) {
            NotifyType.NewStream.value -> {
                val newStream = Gson().fromJson(notify.message, NewStreamNotify::class.java)
                val role = when (newStream.role) {
                    Role.Broadcaster.value -> Role.Broadcaster
                    else -> {
                        Role.Audience
                    }
                }
                val room = IMLiveManager.shared().getRoom()
                room?.let {
                    val audioEnable = it.mode == Mode.Audio || it.mode == Mode.Video
                    val videoEnable = it.mode == Mode.Video
                    val remoteParticipant = RemoteParticipant(
                        newStream.uId,
                        newStream.roomId,
                        role,
                        newStream.streamKey,
                        audioEnable,
                        videoEnable
                    )
                    IMLiveManager.shared().getRoom()?.let {
                        handler.post {
                            it.participantJoin(remoteParticipant)
                        }
                    }
                }

            }

            NotifyType.RemoveStream.value -> {
                val removeStreamNotify =
                    Gson().fromJson(notify.message, RemoveStreamNotify::class.java)
                IMLiveManager.shared().getRoom()?.let {
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
                IMLiveManager.shared().getRoom()?.let {
                    handler.post {
                        it.receivedDcMsg(dataChannelMsg.uId, dataChannelMsg.text)
                    }
                }
            }
        }
    }

    protected fun addVideoTrack(videoTrack: VideoTrack) {
        LLog.d("$uId, addVideoTrack")
        videoTracks.add(videoTrack)
        attach()
    }

    private fun removeVideoTrack(videoTrack: VideoTrack) {
        LLog.d("$uId, removeVideoTrack")
        videoTracks.remove(videoTrack)
        detach()
    }

    protected fun addAudioTrack(audioTrack: AudioTrack) {
        LLog.d("$uId, addAudioTrack")
        audioTracks.add(audioTrack)
    }

    private fun removeAudioTrack(audioTrack: AudioTrack) {
        LLog.d("$uId, removeAudioTrack")
        audioTracks.remove(audioTrack)
    }

    open fun attachViewRender(svr: SurfaceViewRenderer) {
        LLog.d("$uId, attachViewRender $svr")
        if (this.svr != null) {
            detach()
        }
        this.svr = svr
        attach()
    }

    private fun attach() {
        LLog.d("$uId, attach, ${videoTracks.size}, $svr")
        svr?.let {
            if (videoTracks.size > 0) {
                it.init(IMLiveManager.shared().getPCFactoryWrapper().eglCtx, null)
                for (v in videoTracks) {
                    v.addSink(it)
                }
            }
        }

    }

    open fun detachViewRender() {
        LLog.d("$uId ,detachViewRender")
        detach()
    }

    private fun detach() {
        LLog.d("$uId , detach ${this.svr.toString()}")
        svr?.let {
            it.release()
            LLog.d("detach svr ${videoTracks.size}")
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
        LLog.d("$uId, onDisConnected: ")
        detachViewRender()
        videoTracks.clear()
        audioTracks.clear()
        for ((_, v) in dataChannelMap) {
            v.unregisterObserver()
            v.dispose()
        }
        dataChannelMap.clear()
        IMLiveManager.shared().getRoom()?.onParticipantLeave(this)
        handler.removeCallbacksAndMessages(null)
        compositeDisposable.clear()
    }

    abstract fun pushStreamKey(): String?

    abstract fun playStreamKey(): String?

}