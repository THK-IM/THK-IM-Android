package com.thk.im.android.ui.fragment.layout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.fragment.adapter.MessageAdapter
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.disposables.CompositeDisposable

class IMMessageLayout : RecyclerView, IMMsgVHOperator {

    private var hasMore: Boolean = true // 是否有更多消息
    private var count = 20        // 每次加载消息的数量
    private var isLoading = false // 是否正在加载中
    private var hasScrollToBottom = false
    private val disposables = CompositeDisposable()
    private lateinit var msgAdapter: MessageAdapter

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var session: Session
    private var msgPreviewer: IMMsgPreviewer? = null
    private var msgSender: IMMsgSender? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun init(
        lifecycleOwner: LifecycleOwner,
        session: Session,
        sender: IMMsgSender?,
        previewer: IMMsgPreviewer?
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.session = session
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
        msgAdapter = MessageAdapter(session, lifecycleOwner, this)
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

        val gestureDetector = GestureDetector(context, object : OnGestureListener {
            override fun onDown(p0: MotionEvent): Boolean {
                return false
            }

            override fun onShowPress(p0: MotionEvent) {
            }

            override fun onSingleTapUp(p0: MotionEvent): Boolean {
                msgSender?.let {
                    if (it.isKeyboardShowing()) {
                        it.closeKeyboard()
                        return true
                    } else {
                        if (it.closeBottomPanel()) {
                            return true
                        }
                    }
                }
                return false
            }

            override fun onScroll(
                p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float
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

    }

    private fun loadMessages() {
        if (!hasMore || isLoading) return
        val messages = getMessages()
        val startTime = if (messages.isEmpty()) {
            IMCoreManager.commonModule.getSeverTime()
        } else {
            messages.last().cTime
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
                hasMore = t.size >= count
                isLoading = false
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                isLoading = false
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        IMCoreManager.messageModule.queryLocalMessages(session.id, startTime, 0, count)
            .compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    fun scrollToLatestMsg(smooth: Boolean = false) {
        if (smooth) {
            smoothScrollToPosition(0)
        } else {
            scrollToPosition(0)
        }
    }

    fun insertMessages(messages: List<Message>) {
        msgAdapter.insertNews(messages)
    }

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

    fun deleteMessages(deleteMessages: MutableList<Message>) {
        msgAdapter.batchDelete(deleteMessages)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

    override fun onMsgReferContentClick(message: Message, view: View) {
        val position = this.getMessages().indexOf(message)
        if (position > 0) {
            scrollToRow(position)
        } else {
            val messages = getMessages()
            val startTime = if (messages.isEmpty()) {
                IMCoreManager.commonModule.getSeverTime()
            } else {
                messages.last().cTime
            }
            val subscriber = object : BaseSubscriber<List<Message>>() {
                override fun onNext(t: List<Message>) {
                    if (msgAdapter.getMessageCount() == 0) {
                        msgAdapter.setData(t)
                    } else {
                        msgAdapter.addData(t)
                    }
                    val pos = msgAdapter.getMessages().indexOf(message)
                    if (pos > 0) {
                        scrollToRow(position)
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    disposables.remove(this)
                }
            }
            IMCoreManager.messageModule.queryLocalMessages(
                message.sid,
                startTime,
                message.cTime,
                Int.MAX_VALUE
            )
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposables.add(subscriber)
        }
    }

    private fun scrollToRow(row: Int) {
        scrollToPosition(row)
        postDelayed({
            msgAdapter.highlightFlashing(row, 6, this)
        }, 500)
    }

    override fun onMsgCellClick(message: Message, position: Int, view: View) {
        msgPreviewer?.previewMessage(message, position, view)
    }

    override fun onMsgSenderClick(message: Message, position: Int, view: View) {
        val fromUId = message.fUid
        if (fromUId > 0 && fromUId != IMCoreManager.uId) {
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User?) {
                    t?.let {
                        IMUIManager.pageRouter?.openContactUserPage(context, it)
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    disposables.remove(this)
                }

            }
            IMCoreManager.userModule.queryUser(fromUId)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposables.add(subscriber)
        }
    }

    override fun onMsgSenderLongClick(message: Message, pos: Int, it: View) {
        if (session.type != SessionType.Group.value
            && session.type != SessionType.SuperGroup.value
        ) {
            return
        }
        val fromUId = message.fUid
        if (fromUId > 0 && fromUId != IMCoreManager.uId) {
            val subscriber = object : BaseSubscriber<User>() {
                override fun onNext(t: User?) {
                    t?.let {
                        msgSender?.addAtUser(it, null)
                    }
                }

                override fun onComplete() {
                    super.onComplete()
                    disposables.remove(this)
                }

            }
            IMCoreManager.userModule.queryUser(fromUId)
                .compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            disposables.add(subscriber)
        }
    }

    override fun onMsgCellLongClick(message: Message, position: Int, view: View) {
        msgSender?.popupMessageOperatorPanel(view, message)
    }

    override fun onMsgResendClick(message: Message) {
        IMCoreManager.messageModule.resend(message)
    }

    fun getMessages(): List<Message> {
        return (adapter as MessageAdapter).getMessages()
    }

    fun setSelectMode(selected: Boolean, message: Message?) {
        (adapter as MessageAdapter).setSelectMode(selected, message, this)
    }

    override fun isSelectMode(): Boolean {
        return (adapter as MessageAdapter).isSelectMode()
    }

    override fun isItemSelected(message: Message): Boolean {
        return (adapter as MessageAdapter).isItemSelected(message)
    }

    override fun onSelected(message: Message, selected: Boolean) {
        return (adapter as MessageAdapter).onSelected(message, selected)
    }

    override fun readMessage(message: Message) {
        msgSender?.readMessage(message)
    }

    override fun setEditText(text: String) {
        msgSender?.openKeyboard()
        msgSender?.addInputContent(text)
    }

    fun getSelectMessages(): Set<Message> {
        return (adapter as MessageAdapter).getSelectedMessages()
    }

}