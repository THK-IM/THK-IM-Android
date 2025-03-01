package com.thk.im.preview

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import com.gyf.immersionbar.ImmersionBar
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.preview.databinding.ActivityRecordPreviewBinding
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer

class IMRecordPreviewActivity : AppCompatActivity(), IMMsgPreviewer {

    private lateinit var binding: ActivityRecordPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordPreviewBinding.inflate(layoutInflater)
        ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(true).init()
        IMUIManager.uiResourceProvider?.layoutBgColor()?.let {
            binding.clContent.setBackgroundColor(it)
        }
        setContentView(binding.root)
        initEventBus()
        initToolbar()
        initRecordMessagesView()
    }


    private fun initToolbar() {
        val toolbar = binding.tbRecord
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.icon_back)
        val title = intent.getStringExtra("title")
        toolbar.setNavigationOnClickListener {
            finish()
        }
        title?.let {
            toolbar.title = it
        }
    }

    private fun initRecordMessagesView() {
        val recordMessages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("recordMessages", Message::class.java)
        } else {
            intent.getParcelableArrayListExtra("recordMessages")
        }

        val session: Session? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("session", Session::class.java)
        } else {
            intent.getParcelableExtra("session")
        }

        session?.let {
            binding.rcvMessage.init(this, it, null, this)
            recordMessages?.let { messages ->
                binding.rcvMessage.insertMessages(messages)
            }
        }
    }

    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.MsgNew.value, Observer<Message> {
            it?.let {
                for (m in binding.rcvMessage.getMessages()) {
                    if (m.msgId == it.msgId) {
                        binding.rcvMessage.updateMessage(it)
                    }
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgUpdate.value, Observer<Message> {
            it?.let {
                for (m in binding.rcvMessage.getMessages()) {
                    if (m.msgId == it.msgId) {
                        binding.rcvMessage.updateMessage(it)
                    }
                }
            }
        })
    }

    override fun previewMessage(msg: Message, position: Int, originView: View) {
        val interceptor = IMUIManager.getMsgIVProviderByMsgType(msg.type).onMsgBodyClick(
            this, msg, null, originView
        )
        if (interceptor) {
            return
        }
        if (msg.type == MsgType.Image.value || msg.type == MsgType.Video.value) {
            val messages = binding.rcvMessage.getMessages()
            val mediaMessages = ArrayList<Message>()
            var count = 0
            val rightMessages = mutableListOf<Message>()
            for (i in position until messages.size) {
                if (messages[i].type == MsgType.Image.value || messages[i].type == MsgType.Video.value) {
                    rightMessages.add(messages[i])
                    count++
                }
                if (count == 5) {
                    break
                }
            }

            mediaMessages.addAll(rightMessages.reversed())
            count = 0
            for (i in 0 until position) {
                if (messages[position - 1 - i].type == MsgType.Image.value || messages[position - 1 - i].type == MsgType.Video.value) {
                    mediaMessages.add(messages[position - 1 - i])
                    count++
                }
                if (count == 5) {
                    break
                }
            }
            IMUIManager.mediaPreviewer?.previewMediaMessage(
                this, mediaMessages, originView, msg.msgId
            )
        } else if (msg.type == MsgType.Record.value) {
            val originSession: Session? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("originSession", Session::class.java)
                } else {
                    intent.getParcelableExtra("originSession")
                }
            originSession?.let {
                IMUIManager.mediaPreviewer?.previewRecordMessage(this, it, msg)
            }
        }
    }
}