package com.thk.im.android

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.thk.android.im.live.room.Room
import com.thk.android.im.live.LiveManager
import com.thk.android.im.live.room.BaseParticipant
import com.thk.android.im.live.room.Mode
import com.thk.android.im.live.room.Role
import com.thk.android.im.live.room.RoomObserver
import com.thk.im.android.adapter.MessageAdapter
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.base.popup.KeyboardPopupWindow
import com.thk.im.android.databinding.ActivityWebrtcBinding
import com.thk.im.android.ui.utils.IMKeyboardUtils
import com.thk.im.android.view.ParticipantView
import io.reactivex.disposables.CompositeDisposable
import java.nio.ByteBuffer

class WebRtcActivity : AppCompatActivity(), RoomObserver {

    private lateinit var binding: ActivityWebrtcBinding
    private lateinit var roomId: String
    private var room: Room? = null
    private val disposables = CompositeDisposable()
    private val handler = Handler(Looper.getMainLooper())

    private var adapter = MessageAdapter(this)
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private var bottomHeight = 0
    private var keyboardShowing = false
    private fun layoutRefresh(bottomHeight: Int, keyboardShow: Boolean) {
        if (keyboardShow) {
            moveLayout(bottomHeight)
        } else {
            if (keyboardShowing) {
                IMKeyboardUtils.hideSoftInput(binding.etMessage, object : ResultReceiver(handler) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        super.onReceiveResult(resultCode, resultData)
                        moveLayout(bottomHeight, false)
                    }
                })
            } else {
                moveLayout(bottomHeight)
            }
        }
        keyboardShowing = keyboardShow
    }

    private fun moveLayout(bottomHeight: Int, closeBottomPanel: Boolean = true) {
        val animators = AnimatorSet()
        val contentAnimator = ValueAnimator.ofInt(binding.rcvMessages.paddingTop, bottomHeight)
        contentAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Int
            binding.rcvMessages.setPadding(0, animatedValue, 0, 0)
            binding.scParticipants.setPadding(0, animatedValue, 0, 0)
        }
        contentAnimator.duration = 150
        val animator =
            ObjectAnimator.ofFloat(binding.clContent, "translationY", (0 - bottomHeight).toFloat())
        animator.duration = 150
        animators.play(contentAnimator).with(animator)
        animators.start()
    }

    private fun initKeyboardWindow() {
        keyboardPopupWindow = KeyboardPopupWindow(binding.root) {
            LLog.d("$it, $bottomHeight")
            if (it == 0) {
                keyboardShowing = false
            }
            if (bottomHeight != 0) {
                if (it != 0) {
                    layoutRefresh(it, true)
                } else {
                    layoutRefresh(bottomHeight, false)
                }
            } else {
                if (it != 0) {
                    layoutRefresh(it, true)
                } else {
                    layoutRefresh(it, false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebrtcBinding.inflate(layoutInflater)
        initKeyboardWindow()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setContentView(binding.root)
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    if (it.toString().isNotEmpty()) {
                        binding.btnSend.visibility = View.VISIBLE
                    }
                }
            }
        })
        binding.btnSend.visibility = View.INVISIBLE
        binding.btnSend.setOnClickListener {
            room?.let {
                val content = binding.etMessage.text.toString()
                if (it.sendMessage(content)) {
                    binding.etMessage.text = null
                } else {
                    ToastUtils.show("发送失败")
                }
            }
        }
        binding.rcvMessages.adapter = adapter
        binding.rcvMessages.layoutManager = LinearLayoutManager(this)

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
            LiveManager.shared().joinRoom(roomId, Role.Broadcaster, "xxxxxx")
                .compose(RxTransform.flowableToMain()).subscribe(subscriber)
            disposables.add(subscriber)
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
            disposables.add(subscriber)
        }
    }

    private fun onRoomCreatedOrJoined(room: Room) {
        this.room = room
        this.roomId = room.id
        initRoom()
    }

//    private fun testSwitchRole() {
//        handler.postDelayed({
//            switchRole()
//        }, 10000)
//    }

//    private fun switchRole() {
//        room?.let {
//            val role = it.getRole()
//            if (role != null) {
//                if (role == Role.Broadcaster) {
//                    it.setRole(Role.Audience)
//                } else if (role == Role.Audience) {
//                    it.setRole(Role.Broadcaster)
//                }
//            }
//        }
//    }

    private fun initRoom() {
        XXPermissions.with(this).permission(Permission.CAMERA, Permission.RECORD_AUDIO)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    initRoomViews()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    super.onDenied(permissions, doNotAskAgain)
                    ToastUtils.show("permission denied")
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardPopupWindow.dismiss()
        disposables.clear()
        handler.removeCallbacksAndMessages(null)
        LiveManager.shared().getRoom()?.unRegisterObserver(this)
        LiveManager.shared().destroyRoom()
    }

    override fun join(p: BaseParticipant) {
        if (p.roomId == roomId) {
            handler.post {
                addParticipant(p)
            }
        }
    }

    override fun leave(p: BaseParticipant) {
        if (p.roomId == roomId) {
            handler.post {
                removeParticipant(p)
            }
        }
    }

    fun initRoomViews() {
        room?.let {
            it.registerObserver(this@WebRtcActivity)
            val participants = it.getAllParticipants()
            participants.forEach { p ->
                val v = ParticipantView(this)
                binding.llParticipants.addView(v)
                v.setParticipant(p)
            }
        }
    }

    private fun addParticipant(p: BaseParticipant) {
        room?.let {
            if (p.roomId == it.id) {
                val v = ParticipantView(this)
                binding.llParticipants.addView(v)
                v.setParticipant(p)
            }
        }
    }

    private fun removeParticipant(p: BaseParticipant) {
        room?.let {
            if (p.roomId == it.id) {
                var view: ParticipantView? = null
                for (i in 0 until binding.llParticipants.childCount) {
                    val v = binding.llParticipants.getChildAt(i)
                    if (v is ParticipantView) {
                        if (v.getParticipant() == p) {
                            view = v
                            break
                        }
                    }
                }

                view?.let { v ->
                    v.destroy()
                    binding.llParticipants.removeView(v)
                }

            }
        }
    }

    override fun onTextMsgReceived(uid: String, text: String) {
        val content = "user-${uid}: $text"
        adapter.addData(content)
        binding.rcvMessages.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onBufferMsgReceived(bb: ByteBuffer) {
    }


//    private fun setDevices() {
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
//    }


}