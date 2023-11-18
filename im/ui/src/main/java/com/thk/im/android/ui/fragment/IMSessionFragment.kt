package com.thk.im.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.SessionAdapter
import com.thk.im.android.ui.protocol.internal.IMSessionVHOperator
import io.reactivex.disposables.CompositeDisposable


class IMSessionFragment : Fragment(), IMSessionVHOperator {

    interface OnSessionClick {
        fun onSessionClick(session: Session)
    }

    private lateinit var sessionRecyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private val disposables = CompositeDisposable()
    private var hasMore = true
    private val count = 10

    private var sessionClick: OnSessionClick? = null

    fun setSessionClick(sessionClick: OnSessionClick) {
        this.sessionClick = sessionClick
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSessionRecyclerView(view)
        loadSessions()
        initEventBus()
    }

    private fun initSessionRecyclerView(rootView: View) {
        sessionRecyclerView = rootView.findViewById(R.id.rcv_session)
        sessionAdapter = SessionAdapter(this, this)
        sessionRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //当前状态为停止滑动
                    if (!recyclerView.canScrollVertically(1)) {
                        loadSessions()
                    }
                }
            }
        })

        sessionRecyclerView.layoutManager = LinearLayoutManager(context)
        sessionRecyclerView.adapter = sessionAdapter
    }

    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.SessionNew.value, Observer<Session> {
            it?.let {
                sessionAdapter.onNewSession(it)
            }
        })
        XEventBus.observe(this, IMEvent.SessionUpdate.value, Observer<Session> {
            it?.let {
                sessionAdapter.onSessionUpdate(it)
            }
        })
        XEventBus.observe(this, IMEvent.SessionDelete.value, Observer<Session> {
            it?.let {
                sessionAdapter.onSessionRemove(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    private var isLoading = false
    private fun loadSessions() {
        if (!hasMore || isLoading) return
        isLoading = true
        val subscriber = object : BaseSubscriber<List<Session>>() {
            override fun onNext(t: List<Session>) {
                if (sessionAdapter.itemCount == 0) {
                    sessionAdapter.setData(t)
                } else {
                    sessionAdapter.addData(t)
                }
                hasMore = t.size >= count
                isLoading = false
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                isLoading = false
            }
        }
        var current = IMCoreManager.getCommonModule().getSeverTime()
        if (sessionAdapter.getSessionList().isNotEmpty()) {
            current = sessionAdapter.getSessionList().last().mTime
        }
        IMCoreManager.getMessageModule().queryLocalSessions(10, current)
            .compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun updateSession(session: Session) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }

            override fun onError(t: Throwable?) {
                t?.message?.let {
                    LLog.e(it)
                }
            }
        }
        IMCoreManager.getMessageModule().updateSession(session, true)
            .compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun deleteSession(session: Session) {
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onNext(t: Void?) {
            }

            override fun onError(t: Throwable?) {
                t?.message?.let {
                    LLog.e(it)
                }
            }
        }
        IMCoreManager.getMessageModule().deleteSession(session, true)
            .compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun openSession(session: Session) {
        sessionClick?.onSessionClick(session)
    }
}