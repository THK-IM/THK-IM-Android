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
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.ui.fragment.adapter.IMMessageAdapter
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.android.ui.protocol.internal.IMMsgPreviewer
import com.thk.im.android.ui.protocol.internal.IMMsgSender
import com.thk.im.android.ui.protocol.internal.IMMsgVHOperator
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.abs

class IMMessageLayout : RecyclerView, IMMsgVHOperator {

    private var hasMore: Boolean = true // 是否有更多消息
    private var count = 20        // 每次加载消息的数量
    private var isLoading = false // 是否正在加载中
    private var hasScrollToBottom = true
    private val disposables = CompositeDisposable()
    private lateinit var msgAdapter: IMMessageAdapter

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
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        layoutManager = linearLayoutManager
        msgAdapter = IMMessageAdapter(session, lifecycleOwner, this)
        adapter = msgAdapter

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) { //当前状态为停止滑动
                    if (!recyclerView.canScrollVertically(-1)) {
                        loadMessages()
                    }
                    val offset =
                        recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent() - recyclerView.computeVerticalScrollOffset()
                    if (abs(offset) < 50) {
                        msgSender?.showNewMsgTipsView(true)
                    }
                    hasScrollToBottom = abs(offset) <= 200
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

    fun loadMessages() {
        if (!hasMore || isLoading) return
        val messages = getMessages()
        val endTime = if (messages.isEmpty()) {
            IMCoreManager.severTime
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
        IMCoreManager.messageModule.queryLocalMessages(session.id, 0, endTime, count)
            .compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    fun scrollToUnReadMsg() {
        val subscriber = object : BaseSubscriber<Message>() {
            override fun onNext(t: Message?) {
                t?.let {
                    scrollToMsg(it)
                }
            }
        }

        Flowable.just(session)
            .flatMap {
                val message = IMCoreManager.db.messageDao().findOldestUnreadMessage(it.id)
                return@flatMap Flowable.just(message)
            }.compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        this.disposables.add(subscriber)
    }

    fun scrollToLatestMsg(smooth: Boolean = false) {
        if (smooth) {
            smoothScrollToPosition(0)
        } else {
            scrollToPosition(0)
        }
    }

    private fun indexOfMessage(msg: Message): Int {
        val messages = msgAdapter.getMessages()
        var pos = 0
        while (pos < messages.size) {
            if (messages[pos].msgId == msg.msgId) {
                return pos
            }
            pos++
        }
        return -1
    }

    fun scrollToMsg(msg: Message, flashing: Boolean = false) {
        var index = indexOfMessage(msg)
        if (index != -1) {
            scrollToRow(index, flashing)
        } else {
            val subscriber = object : BaseSubscriber<List<Message>>() {
                override fun onNext(t: List<Message>?) {
                    t?.let {
                        msgAdapter.addData(it)
                        index = indexOfMessage(msg)
                        if (index != -1) {
                            scrollToRow(index, flashing)
                        }
                    }
                }
            }
            val startTime = msg.cTime
            val messages = msgAdapter.getMessages()
            var endTime = 0L
            if (messages.isNotEmpty()) {
                endTime = messages.last().cTime
            }
            IMCoreManager.messageModule.queryLocalMessages(
                session.id,
                startTime,
                endTime,
                Int.MAX_VALUE
            ).compose(RxTransform.flowableToMain())
                .subscribe(subscriber)
            this.disposables.add(subscriber)
        }
    }

    private fun scrollToRow(row: Int, flashing: Boolean = false) {
        scrollToPosition(row)
        if (flashing) {
            postDelayed({
                msgAdapter.highlightFlashing(row, 6, this)
            }, 1000)
        }
    }

    fun refreshMessageUserInfo() {
        msgAdapter.updateUserInfo()
    }

    fun insertMessages(messages: List<Message>) {
        msgAdapter.insertNews(messages)
    }

    fun insertMessage(message: Message) {
        val pos = msgAdapter.insertNew(message)
        if (pos == 0) {
            if (hasScrollToBottom || message.fUid == IMCoreManager.uId) {
                scrollToLatestMsg()
            } else {
                msgSender?.showNewMsgTipsView(false)
            }
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

    fun clearMessages() {
        msgAdapter.clear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

    override fun onMsgReferContentClick(message: Message, view: View) {
        scrollToMsg(message, true)
    }


    override fun onMsgCellClick(message: Message, position: Int, view: View) {
        msgPreviewer?.previewMessage(message, position, view)
    }

    override fun onMsgSenderClick(message: Message, position: Int, view: View) {
        val subscriber = object : BaseSubscriber<User>() {
            override fun onNext(t: User?) {
                t?.let {
                    IMUIManager.pageRouter?.openUserPage(context, it, session)
                }
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }

        }
        IMCoreManager.userModule.queryUser(message.fUid)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun onMsgReadStatusClick(message: Message) {
        IMUIManager.pageRouter?.openMsgReadStatusPage(context, session, message)
    }

    override fun onMsgSenderLongClick(message: Message, pos: Int, it: View) {
        if (session.type != SessionType.Group.value
            && session.type != SessionType.SuperGroup.value
        ) {
            return
        }
        val fromUId = message.fUid
        if (fromUId > 0 && fromUId != IMCoreManager.uId) {
            msgSender?.let {
                val subscriber = object : BaseSubscriber<Pair<User, SessionMember?>>() {
                    override fun onNext(t: Pair<User, SessionMember?>?) {
                        t?.let { p ->
                            msgSender?.addAtUser(p.first, p.second)
                        }
                    }

                    override fun onComplete() {
                        super.onComplete()
                        disposables.remove(this)
                    }

                }
                it.asyncGetSessionMemberInfo(fromUId)
                    .compose(RxTransform.flowableToMain())
                    .subscribe(subscriber)
                disposables.add(subscriber)
            }
        }
    }

    override fun onMsgCellLongClick(message: Message, position: Int, view: View) {
        msgSender?.popupMessageOperatorPanel(view, message)
    }

    override fun onMsgResendClick(message: Message) {
        msgSender?.resendMessage(message)
    }

    fun getMessages(): List<Message> {
        return (adapter as IMMessageAdapter).getMessages()
    }

    fun setSelectMode(selected: Boolean, message: Message?) {
        (adapter as IMMessageAdapter).setSelectMode(selected, message)
    }

    override fun isSelectMode(): Boolean {
        return (adapter as IMMessageAdapter).isSelectMode()
    }

    override fun isItemSelected(message: Message): Boolean {
        return (adapter as IMMessageAdapter).isItemSelected(message)
    }

    override fun onSelected(message: Message, selected: Boolean) {
        return (adapter as IMMessageAdapter).onSelected(message, selected)
    }

    override fun msgSender(): IMMsgSender? {
        return msgSender
    }

    fun getSelectMessages(): Set<Message> {
        return (adapter as IMMessageAdapter).getSelectedMessages()
    }
}