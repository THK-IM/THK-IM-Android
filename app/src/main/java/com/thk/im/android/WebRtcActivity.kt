package com.thk.im.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.api.BaseSubscriber
import com.thk.android.im.live.api.RxTransform
import com.thk.android.im.live.participant.BaseParticipant
import com.thk.android.im.live.room.Mode
import com.thk.android.im.live.room.Role
import com.thk.android.im.live.room.Room
import com.thk.android.im.live.room.RoomObserver
import com.thk.im.android.adapter.ParticipantAdapter
import com.thk.im.android.common.ToastUtils
import com.thk.im.android.databinding.ActivityWebrtcBinding
import io.reactivex.disposables.CompositeDisposable

class WebRtcActivity : AppCompatActivity(), RoomObserver {

    private lateinit var binding: ActivityWebrtcBinding
    private lateinit var adapter: ParticipantAdapter
    private lateinit var roomId: String
    private var room: Room? = null
    private val disposables = CompositeDisposable()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebrtcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        roomId = intent.getStringExtra("room_id") ?: ""
        if (roomId != "") {
            val subscriber = object : BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        onRoomCreatedOrJoined(it)
                    }
                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                    ToastUtils.show("加入失败")
                    finish()
                }
            }
            LiveManager.shared().joinRoom(roomId, Role.Audience, "xxxxxx")
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposables.addAll(subscriber)
        } else {
            val subscriber = object : BaseSubscriber<Room>() {
                override fun onNext(t: Room?) {
                    t?.let {
                        room = t
                        onRoomCreatedOrJoined(it)
                    }
                }

                override fun onError(t: Throwable?) {
                    super.onError(t)
                    ToastUtils.show("创建失败")
                    finish()
                }
            }
            LiveManager.shared().createRoom(Mode.Video).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposables.addAll(subscriber)
        }
    }

    private fun onRoomCreatedOrJoined(room: Room) {
        this.room = room
        this.roomId = room.id
        initRoom()
    }

    private fun testSwitchRole() {
        handler.postDelayed({
            switchRole()
        }, 10000)
    }

    private fun switchRole() {
        room?.let {
            val role = it.getRole()
            if (role != null) {
                if (role == Role.Broadcaster) {
                    it.setRole(Role.Audience)
                } else if (role == Role.Audience) {
                    it.setRole(Role.Broadcaster)
                }
            }
        }
    }

    private fun initRoom() {
        XXPermissions.with(this)
            .permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    adapter = ParticipantAdapter(this@WebRtcActivity)
                    binding.rcvParticipants.layoutManager =
                        LinearLayoutManager(this@WebRtcActivity, RecyclerView.VERTICAL, false)
                    binding.rcvParticipants.adapter = adapter
                    room?.let {
                        it.registerObserver(this@WebRtcActivity)
                        val participants = it.getAllParticipants()
                        adapter.setData(participants)
                    }
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    super.onDenied(permissions, never)
                    ToastUtils.show("permission denied")
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        handler.removeCallbacksAndMessages(null)
        LiveManager.shared().getRoom()?.unRegisterObserver(this)
        LiveManager.shared().destroyRoom()
    }

    override fun join(p: BaseParticipant) {
        if (p.roomId == roomId) {
            handler.post {
                adapter.addData(p)
            }
        }
    }

    override fun leave(p: BaseParticipant) {
        if (p.roomId == roomId) {
            handler.post {
                adapter.remove(p)
            }
        }
    }


    private fun setDevices() {
//        val devices = LiveManager.shared().getPCFactoryWrapper().getAudioInputDevice()
//        Log.v(
//            "Device",
//            "size ${devices.size}"
//        )
//        for (device in devices) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                Log.v(
//                    "Device",
//                    device.productName.toString() + ", " + device.type + ", " + device.toString()
//                )
//            }
//        }
    }


}