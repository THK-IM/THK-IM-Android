package com.thk.im.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.R
import com.thk.im.android.ui.fragment.adapter.SessionAdapter
import io.reactivex.disposables.CompositeDisposable


class SessionFragment : Fragment() {

    interface OnSessionClick {
        fun onSessionClick(session: Session)
    }

    private lateinit var sessionRecyclerView: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private val composite = CompositeDisposable()
    private var hasMore = true
    private val count = 5

    private var sessionClick: OnSessionClick? = null

    fun setSessionClick(sessionClick: OnSessionClick) {
        this.sessionClick = sessionClick
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        sessionAdapter = SessionAdapter(this)
        sessionAdapter.onItemClickListener = object : SessionAdapter.OnItemClickListener {
            override fun onItemClick(adapter: SessionAdapter, position: Int, session: Session) {
                sessionClick?.onSessionClick(session)
            }

            override fun onItemLongClick(
                adapter: SessionAdapter,
                position: Int,
                session: Session
            ): Boolean {
                return true
            }
        }
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
        XEventBus.observe(this, XEventType.SessionNew.value, Observer<Session> {
            it?.let {
                sessionAdapter.insertNew(it)
            }
        })
        XEventBus.observe(this, XEventType.SessionUpdate.value, Observer<Session> {
            it?.let {
                sessionAdapter.update(it)
            }
        })
        XEventBus.observe(this, XEventType.SessionDeleted.value, Observer<Session> {
            it?.let {
                sessionAdapter.delete(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composite.clear()
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
        val current = IMCoreManager.getSignalModule().severTime
        IMCoreManager.getMessageModule().queryLocalSessions(sessionAdapter.itemCount, current)
            .subscribe(subscriber)
        composite.add(subscriber)
    }
}