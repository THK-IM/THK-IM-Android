package com.thk.im.android.ui.fragment.popup

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
import com.thk.im.android.ui.fragment.popup.adapter.IMOnSessionMemberClick
import com.thk.im.android.ui.fragment.popup.adapter.IMSessionMemberAdapter
import io.reactivex.disposables.CompositeDisposable

class IMAtSessionMemberPopup constructor(
    context: Context,
    private val session: Session,
    private val inputOperator: IMInputOperator,
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
            override fun onSessionMemberClick(sessionMember: SessionMember, user: User) {
                dismiss()
                inputOperator.insertAtSessionMember(sessionMember, user)
            }
        }
        rcvSessionMember.adapter = sessionMemberAdapter
        initData()
    }

    private fun initData() {
        val subscriber = object : BaseSubscriber<List<SessionMember>>() {
            override fun onNext(t: List<SessionMember>?) {
                t?.let {
                    sessionMemberAdapter.setData(it)
                }
            }

            override fun onComplete() {
                super.onComplete()
                compositeDisposable.remove(this)
            }
        }
        IMCoreManager.messageModule.querySessionMembers(session.id)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}