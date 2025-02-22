package com.thk.im.android.ui.fragment.popup

import android.content.Context
import android.graphics.Color
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.lxj.xpopup.core.BottomPopupView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.IMSessionFragment
import com.thk.im.android.ui.manager.IMRecordMsgBody
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import io.reactivex.Flowable

class IMSessionChoosePopup(
    context: Context,
) : BottomPopupView(context) {

    var session: Session? = null
    var messages: List<Message>? = null
    var sender: IMMsgSender? = null
    var forwardType: Int = 0 // 0 单条转发，1合并转发


    private val fragmentTag = "ChooseSession"
    override fun getImplLayoutId(): Int {
        return R.layout.popup_choose_session
    }

    override fun onCreate() {
        super.onCreate()
        if (session == null || messages == null || sender == null) {
            dismiss()
            return
        }
        val inputBgColor =
            IMUIManager.uiResourceProvider?.inputBgColor() ?: Color.parseColor("#EEEEEE")
        findViewById<ConstraintLayout>(R.id.ly_root).setBackgroundColor(inputBgColor)
        initToolbar()
        val supportFragmentManager = (context as FragmentActivity).supportFragmentManager
        var sessionFragment =
            supportFragmentManager.findFragmentByTag(fragmentTag) as? IMSessionFragment
        if (sessionFragment == null) {
            sessionFragment = IMSessionFragment()
        }
        sessionFragment.setSessionClick(object : IMSessionFragment.OnSessionClick {
            override fun onSessionClick(session: Session) {
                forward(session)
            }
        })
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_session, sessionFragment, fragmentTag)
            .commit()
    }

    override fun onDismiss() {
        super.onDismiss()
        val supportFragmentManager = (context as FragmentActivity).supportFragmentManager
        val sessionFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (sessionFragment != null) {
            supportFragmentManager.beginTransaction().remove(sessionFragment).commit()
        }
    }

    private fun initToolbar() {
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")
        val toolbar = findViewById<Toolbar>(R.id.tb_session_choose)
        val drawable = AppCompatResources.getDrawable(context, R.drawable.icon_back)
        drawable?.setTint(inputTextColor)
        toolbar.navigationIcon = drawable
        toolbar.title = context.getString(R.string.choose_one_session)
        toolbar.setTitleTextColor(inputTextColor)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun forward(session: Session) {
        if (messages == null) {
            return
        }
        if (forwardType == 0) {
            for (m in messages!!) {
                IMCoreManager.messageModule.getMsgProcessor(m.type)
                    .forwardMessage(m, session.id)
            }
        } else {
            val subscriber = object : BaseSubscriber<IMRecordMsgBody>() {
                override fun onNext(t: IMRecordMsgBody) {
                    val cleanMessages = mutableListOf<Message>()
                    for (m in t.messages) {
                        val msg = m.copy()
                        msg.sendStatus = MsgSendStatus.Success.value
                        msg.oprStatus =
                            MsgOperateStatus.Ack.value.or(MsgOperateStatus.ClientRead.value)
                                .or(MsgOperateStatus.ServerRead.value)
                        msg.rUsers = null
                        msg.data = null
                        cleanMessages.add(msg)
                    }
                    val body = IMRecordMsgBody(t.title, cleanMessages, t.content)
                    IMCoreManager.messageModule
                        .sendMessage(session.id, MsgType.Record.value, body, null)
                }
            }
            buildRecordBody(messages!!).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
        }
        dismiss()
    }

    private fun buildRecordBody(messages: List<Message>): Flowable<IMRecordMsgBody> {
        return Flowable.just(IMRecordMsgBody("", messages, ""))
            .flatMap {
                var content = ""
                var i = 0
                for (subMessage in messages) {
                    var userName = "XX"
                    val memberInfo = sender!!.syncGetSessionMemberInfo(subMessage.fUid)
                    memberInfo?.let { info ->
                        userName =
                            if (info.second?.noteName != null && info.second?.noteName != "") {
                                info.second?.noteName!!
                            } else {
                                info.first.nickname
                            }
                    }
                    val subContent = IMCoreManager.messageModule.getMsgProcessor(subMessage.type)
                        .msgDesc(subMessage)
                    content = content.plus("${userName}:${subContent}")
                    if (i > 5) {
                        break
                    }
                    i++
                    if (i <= messages.size - 1) {
                        content = content.plus("\n")
                    }
                }
                it.content = content
                Flowable.just(it)
            }.flatMap {
                val memberInfo = sender!!.syncGetSessionMemberInfo(IMCoreManager.uId)
                memberInfo?.let { info ->
                    it.title = if (info.second?.noteName != null && info.second?.noteName != "") {
                        info.second?.noteName!!
                    } else {
                        info.first.nickname
                    }
                }
                Flowable.just(it)
            }.flatMap {
                val title =
                    if (session?.type == SessionType.Group.value || session?.type == SessionType.SuperGroup.value) {
                        context.getString(R.string.someone_s_group_chat_record)
                    } else {
                        context.getString(R.string.someone_s_chat_record)
                    }
                it.title = String.format(title, it.title)
                return@flatMap Flowable.just(it)
            }
    }
}