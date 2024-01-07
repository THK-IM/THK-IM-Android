package com.thk.im.android.ui.fragment.popup

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.lxj.xpopup.core.BottomPopupView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.IMSessionFragment
import com.thk.im.android.ui.manager.IMRecordMsgBody
import io.reactivex.Flowable

class IMSessionChoosePopup constructor(
    context: Context,
    private val session: Session,
    private val messages: List<Message>,
    private val forwardType: Int // 0 单条转发，1合并转发
) : BottomPopupView(context) {

    private val fragmentTag = "ChooseSession"
    override fun getImplLayoutId(): Int {
        return R.layout.popup_choose_session
    }

    override fun onCreate() {
        super.onCreate()
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
            .add(R.id.fl_session, sessionFragment, fragmentTag)
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
        val toolbar = findViewById<Toolbar>(R.id.tb_session_choose)
        toolbar.navigationIcon = AppCompatResources.getDrawable(context, R.drawable.icon_back)
        toolbar.title = "选择一个聊天"
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    private fun forward(session: Session) {
        if (forwardType == 0) {
            for (m in messages) {
                IMCoreManager.messageModule.getMsgProcessor(m.type)
                    .forwardMessage(m, session.id)
            }
        } else {
            val subscriber = object : BaseSubscriber<IMRecordMsgBody>() {
                override fun onNext(t: IMRecordMsgBody) {
                    val cleanMessages = mutableListOf<Message>()
                    for (m in t.messages) {
                        val msg = m.copy()
                        msg.sendStatus = 0
                        msg.oprStatus = 0
                        msg.rUsers = null
                        msg.data = null
                        cleanMessages.add(msg)
                    }
                    val body = IMRecordMsgBody(t.title, cleanMessages, t.content)
                    IMCoreManager.messageModule
                        .sendMessage(session.id, MsgType.RECORD.value, body, null)
                }
            }
            buildRecordBody().compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
        }
        dismiss()
    }

    private fun buildRecordBody(): Flowable<IMRecordMsgBody> {
        val uIds = mutableSetOf<Long>()
        for (subMessage in messages) {
            uIds.add(subMessage.fUid)
        }
        return IMCoreManager.userModule.queryUsers(uIds).flatMap {
            var content = ""
            var i = 0
            for (subMessage in messages) {
                val userName = it[subMessage.fUid]?.nickname ?: "XX"
                val subContent = IMCoreManager.messageModule
                    .getMsgProcessor(subMessage.type).getSessionDesc(subMessage)
                content = content.plus("${userName}:${subContent}")
                i++
                if (i <= messages.size - 1) {
                    content = content.plus("\n")
                }
            }
            val recordMsgBody = IMRecordMsgBody("", messages.toList(), content)
            return@flatMap Flowable.just(recordMsgBody)
        }.flatMap {
            return@flatMap IMCoreManager.userModule.queryUser(IMCoreManager.uId)
                .flatMap { user ->
                    it.title = user.nickname
                    return@flatMap Flowable.just(it)
                }
        }.flatMap {
            val title = if (session.type == SessionType.Group.value) {
                "的群聊记录"
            } else {
                "的会话记录"
            }
            it.title = "${it.title}${title}"
            return@flatMap Flowable.just(it)
        }
    }
}