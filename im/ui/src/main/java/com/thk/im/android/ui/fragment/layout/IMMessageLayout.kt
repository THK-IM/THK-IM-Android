package com.thk.im.android.ui.fragment.layout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.db.entity.Message
import com.thk.im.android.ui.fragment.adapter.MessageAdapter
import com.thk.im.android.ui.protocol.IMMsgPreviewer
import com.thk.im.android.ui.protocol.IMMsgSender
import com.thk.im.android.ui.protocol.IMMsgVHOperator
import io.reactivex.disposables.CompositeDisposable

class IMMessageLayout : RecyclerView, IMMsgVHOperator {

    private var cTime: Long = 0L  // 根据创建时间加载消息
    private var hasMore: Boolean = true // 是否有更多消息
    private var count = 20        // 每次加载消息的数量
    private var isLoading = false // 是否正在加载中
    private var hasScrollToBottom = false
    private val disposables = CompositeDisposable()
    private lateinit var msgAdapter: MessageAdapter

    private lateinit var msgSender: IMMsgSender
    private lateinit var msgPreviewer: IMMsgPreviewer
    private lateinit var fragment: Fragment

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    fun init(fragment: Fragment, sender: IMMsgSender, previewer: IMMsgPreviewer) {
        this.fragment = fragment
        this.msgSender = sender
        this.msgPreviewer = previewer
        initUI()
        loadMessages()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        layoutManager = linearLayoutManager
        val session = msgSender.getSession()
        msgAdapter = MessageAdapter(session, fragment, this)
        adapter = msgAdapter

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) { //当前状态为停止滑动
                    if (!recyclerView.canScrollVertically(-1)) {
                        loadMessages()
                    }
                    hasScrollToBottom = !recyclerView.canScrollVertically(1)
                }
            }
        })

        linearLayoutManager.stackFromEnd = true

        val gestureDetector = GestureDetector(context, object : OnGestureListener {
            override fun onDown(p0: MotionEvent): Boolean {
                return false
            }

            override fun onShowPress(p0: MotionEvent) {
            }

            override fun onSingleTapUp(p0: MotionEvent): Boolean {
                if (msgSender.isKeyboardShowing()) {
                    msgSender.closeKeyboard()
                    return true
                } else {
                    if (msgSender.closeBottomPanel()) {
                        return true
                    }
                }
                return false
            }

            override fun onScroll(
                p0: MotionEvent?,
                p1: MotionEvent,
                p2: Float,
                p3: Float
            ): Boolean {
                return false
            }

            override fun onLongPress(p0: MotionEvent) {
            }

            override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
                return false
            }

        })

        setOnTouchListener { _, p1 ->
            if (p1 != null) {
                gestureDetector.onTouchEvent(p1)
            } else {
                false
            }
        }

//        binding.rcvMessage.setOnTouchListener { _, _ ->
//            if (bottomHeight > 0 || keyboardShowing) {
//                bottomHeight = 0
//                layoutRefresh(bottomHeight, false)
//                true
//            } else {
//                false
//            }
//        }
    }

    private fun loadMessages() {
        if (!hasMore || isLoading) return
        if (msgAdapter.getMessageCount() == 0) {
            cTime = IMCoreManager.signalModule.severTime
        }
        isLoading = true
        val subscriber = object : BaseSubscriber<List<Message>>() {
            override fun onNext(t: List<Message>) {
                if (msgAdapter.getMessageCount() == 0) {
                    msgAdapter.setData(t)
                    scrollToLatestMsg()
                } else {
                    msgAdapter.addData(t)
                }
                if (t.isNotEmpty()) {
                    cTime = t[t.size - 1].cTime
                }
                hasMore = t.size >= count
                isLoading = false
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                isLoading = false
            }
        }
        val session = msgSender.getSession()
        IMCoreManager.getMessageModule().queryLocalMessages(session.id, cTime, count)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    fun scrollToLatestMsg(smooth: Boolean = false) {
        if (smooth) {
            smoothScrollToPosition(0)
        } else {
            scrollToPosition(0)
        }
    }

//    private fun getMessageContentHeight(): Int {
//        var totalHeight = 0
//        for (i in 0 until msgAdapter.itemCount) {
//            val itemView: View? = layoutManager?.findViewByPosition(i)
//            itemView?.let { iv ->
//                iv.measure(MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
//                totalHeight += iv.measuredHeight
//            }
//        }
//        return totalHeight
//    }

    fun insertMessage(message: Message) {
        val pos = msgAdapter.insertNew(message)
        if (pos == 0) {
            scrollToLatestMsg()
        }
    }

    fun updateMessage(message: Message) {
        msgAdapter.update(message)
    }

    fun deleteMessage(message: Message) {
        msgAdapter.delete(message)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

    override fun onMsgCellClick(message: Message, position: Int, view: View) {
        msgPreviewer.previewMessage(message, position, view)
    }

    override fun onMsgCellLongClick(message: Message, position: Int, view: View) {
    }

    override fun onMsgResendClick(message: Message) {
        IMCoreManager.getMessageModule().resend(message)
    }

    fun getMessages(): List<Message> {
        return (adapter as MessageAdapter).getMessages()
    }

}