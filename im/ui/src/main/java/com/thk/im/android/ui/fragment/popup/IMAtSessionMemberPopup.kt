package com.thk.im.android.ui.fragment.popup

import android.content.Context
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji2.widget.EmojiTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.core.BottomPopupView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.IMOnSessionMemberClick
import com.thk.im.android.ui.fragment.adapter.IMSessionMemberAdapter
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMSessionMemberAtDelegate
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

class IMAtSessionMemberPopup(
    context: Context,
) : BottomPopupView(context) {

    var session: Session? = null
    var sessionMemberAtDelegate: IMSessionMemberAtDelegate? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var rcvSessionMember: RecyclerView
    private lateinit var titleView: EmojiTextView
    private lateinit var sessionMemberAdapter: IMSessionMemberAdapter

    override fun getImplLayoutId(): Int {
        return R.layout.popup_at_session_member
    }

    override fun onCreate() {
        super.onCreate()
        if (session == null || sessionMemberAtDelegate == null) {
            dismiss()
            return
        }

        val bgLayoutColor =
            IMUIManager.uiResourceProvider?.panelBgColor() ?: Color.parseColor("#FFFFFF")
        val inputTextColor =
            IMUIManager.uiResourceProvider?.inputTextColor() ?: Color.parseColor("#333333")

        rootLayout = findViewById(R.id.ly_root)
        titleView = findViewById(R.id.tv_title)
        rcvSessionMember = findViewById(R.id.rcv_session_member)
        titleView.text = context.getString(R.string.choose_at_people)
        rootLayout.setBackgroundColor(bgLayoutColor)
        titleView.setTextColor(inputTextColor)

        rcvSessionMember.layoutManager = LinearLayoutManager(context)
        sessionMemberAdapter = IMSessionMemberAdapter()
        sessionMemberAdapter.onSessionMemberClick = object : IMOnSessionMemberClick {
            override fun onSessionMemberClick(user: User, sessionMember: SessionMember?) {
                dismiss()
                sessionMemberAtDelegate?.onSessionMemberAt(user, sessionMember)
            }
        }
        rcvSessionMember.adapter = sessionMemberAdapter
        initData()
    }

    private fun initData() {
        fetchSessionMembers()
    }

    private fun fetchSessionMembers() {
        if (session == null) {
            return
        }
        val subscriber = object : BaseSubscriber<Map<Long, Pair<User, SessionMember?>>>() {
            override fun onNext(t: Map<Long, Pair<User, SessionMember?>>?) {
                t?.let {
                    updateSessionMember(it)
                }
                compositeDisposable.remove(this)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                compositeDisposable.remove(this)
            }
        }
        IMCoreManager.messageModule.querySessionMembers(session!!.id, false)
            .flatMap { members ->
                val uIds = mutableSetOf<Long>()
                for (m in members) {
                    if (m.deleted == 0) {
                        uIds.add(m.userId)
                    }
                }
                return@flatMap IMCoreManager.userModule.queryUsers(uIds).flatMap { userMap ->
                    val memberMap = mutableMapOf<Long, Pair<User, SessionMember?>>()
                    for ((k, v) in userMap) {
                        for (m in members) {
                            if (m.userId == k) {
                                val pair = Pair(v, m)
                                memberMap[k] = pair
                                break
                            }
                        }
                    }
                    Flowable.just(memberMap)
                }
            }.compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    private fun updateSessionMember(it: Map<Long, Pair<User, SessionMember?>>) {
        val members = mutableListOf<Pair<User, SessionMember?>>()
        members.addAll(it.values)
        session?.let {
            val canAtAll = IMUIManager.uiResourceProvider?.canAtAll(it) ?: false
            if (canAtAll) {
                members.add(Pair(User.all, null))
            }
        }
        sessionMemberAdapter.setData(members)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}