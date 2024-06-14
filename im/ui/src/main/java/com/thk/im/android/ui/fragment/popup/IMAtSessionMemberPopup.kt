package com.thk.im.android.ui.fragment.popup

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.Toolbar
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
import com.thk.im.android.ui.protocol.internal.IMSessionMemberAtDelegate
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

@SuppressLint("ViewConstructor")
class IMAtSessionMemberPopup(
    context: Context,
    private val session: Session,
    private val sessionMemberAtDelegate: IMSessionMemberAtDelegate,
) : BottomPopupView(context) {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var rcvSessionMember: RecyclerView
    private lateinit var toolbar : Toolbar
    private lateinit var sessionMemberAdapter: IMSessionMemberAdapter

    override fun getImplLayoutId(): Int {
        return R.layout.popup_at_session_member
    }

    override fun onCreate() {
        super.onCreate()
        toolbar = findViewById(R.id.tb_session_member_choose)
        toolbar.title = "选择提醒的人"
        rcvSessionMember = findViewById(R.id.rcv_session_member)
        rcvSessionMember.layoutManager = LinearLayoutManager(context)
        sessionMemberAdapter = IMSessionMemberAdapter()
        sessionMemberAdapter.onSessionMemberClick = object : IMOnSessionMemberClick {
            override fun onSessionMemberClick(user: User, sessionMember: SessionMember?) {
                dismiss()
                sessionMemberAtDelegate.onSessionMemberAt(user, sessionMember)
            }
        }
        rcvSessionMember.adapter = sessionMemberAdapter
        initData()
    }

    private fun initData() {
        fetchSessionMembers()
    }

    private fun fetchSessionMembers() {
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
        IMCoreManager.messageModule.querySessionMembers(session.id, false)
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
        members.add(Pair(User.all, null))
        sessionMemberAdapter.setData(members)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}