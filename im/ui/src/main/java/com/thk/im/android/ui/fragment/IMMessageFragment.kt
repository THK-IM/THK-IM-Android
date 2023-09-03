package com.thk.im.android.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.text.Editable
import android.text.Selection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.base.BaseSubscriber
import com.thk.im.android.base.LLog
import com.thk.im.android.base.RxTransform
import com.thk.im.android.base.extension.dp2px
import com.thk.im.android.base.popup.KeyboardPopupWindow
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.db.MsgType
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.databinding.FragmentImMessageBinding
import com.thk.im.android.ui.fragment.adapter.MessageAdapter
import com.thk.im.android.ui.panel.component.ComponentPanel
import com.thk.im.android.ui.panel.component.ComponentViewHolder
import com.thk.im.android.ui.panel.component.internal.BaseComponentViewHolder
import com.thk.im.android.ui.panel.component.internal.UIComponentManager
import com.thk.im.android.ui.panel.emoji.EmojiPanel
import com.thk.im.android.ui.panel.emoji.EmojiPanelCallback
import com.thk.im.android.ui.utils.IMKeyboardUtils
import io.reactivex.disposables.CompositeDisposable

class IMMessageFragment : Fragment(), EmojiPanelCallback {
    private lateinit var keyboardPopupWindow: KeyboardPopupWindow
    private lateinit var msgAdapter: MessageAdapter
    private lateinit var emojiPanel: EmojiPanel
    private lateinit var featurePanel: ComponentPanel
    private val composite = CompositeDisposable()
    private val sid: Long by lazy {
        session.id
    }
    private val session: Session by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return@lazy arguments?.getParcelable("session", Session::class.java) as Session
        } else {
            return@lazy arguments?.getParcelable<Session?>("session")!!
        }
    }
    private var _binding: FragmentImMessageBinding? = null
    private val binding get() = _binding!!
    private var bottomHeight = 0

    private val uiComponentManager =
        UIComponentManager(object : UIComponentManager.IViewHolderProvider {
            override fun provideViewHolder(parent: ViewGroup): BaseComponentViewHolder {
                return ComponentViewHolder.create(parent)
            }

        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImMessageBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        keyboardPopupWindow.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        initKeyboardWindow()
        initMessageRecyclerView(view)
        initEventBus()
        loadMessages()
        binding.tvSendMsg.setOnClickListener {
            binding.etMessage.text?.let {
                IMCoreManager.getMessageModule().getMsgProcessor(MsgType.TEXT.value)
                    .sendMessage(
                        it.toString(), sid,
                    )
            }
            binding.etMessage.text = null
        }

        binding.etMessage.addTextChangedListener {
            if ((it?.length ?: 0) > 0) {
                binding.tvSendMsg.visibility = View.VISIBLE
                binding.ivAddMore.visibility = View.GONE
            } else {
                binding.tvSendMsg.visibility = View.GONE
                binding.ivAddMore.visibility = View.VISIBLE
            }
        }

        binding.ivEmo.setOnClickListener {
            binding.ivVoice.isSelected = false
            binding.ivAddMore.isSelected = false
            binding.btRecordVoice.visibility = View.GONE
            binding.etMessage.visibility = View.VISIBLE
            binding.ivEmo.isSelected = !binding.ivEmo.isSelected
            if (binding.ivEmo.isSelected) {
                featurePanel.hide()
                emojiPanel.show()
                bottomHeight = 300.dp2px()
                layoutRefresh(bottomHeight, false)
            } else {
                bottomHeight = 0
                IMKeyboardUtils.showSoftInput(binding.etMessage)
            }
        }

        binding.ivAddMore.setOnClickListener {
            binding.ivVoice.isSelected = false
            binding.ivEmo.isSelected = false
            binding.btRecordVoice.visibility = View.GONE
            binding.etMessage.visibility = View.VISIBLE
            binding.ivAddMore.isSelected = !binding.ivAddMore.isSelected
            if (binding.ivAddMore.isSelected) {
                featurePanel.show()
                emojiPanel.hide()
                bottomHeight = 200.dp2px()
                layoutRefresh(bottomHeight, false)
            } else {
                if (keyboardPopupWindow.keyboardHeight > 0) {
                    IMKeyboardUtils.showSoftInput(binding.etMessage)
                } else {
                    bottomHeight = 0
                    layoutRefresh(bottomHeight, false)
                }
            }
        }

        emojiPanel = EmojiPanel(requireActivity(), binding.clEmoji, this)
        featurePanel = ComponentPanel(requireActivity(), binding.clFeature, uiComponentManager)
    }

    private fun initKeyboardWindow() {
        keyboardPopupWindow = KeyboardPopupWindow(binding.root) {
            Log.v("IMMessageFragment", "$it, $bottomHeight")
            if (it == 0) {
                keyboardShowing = false
            }
            if (bottomHeight != 0) {
                if (it != 0) {
                    layoutRefresh(it, true)
                } else {
                    layoutRefresh(bottomHeight, false)
                }
            } else {
                if (it != 0) {
                    layoutRefresh(it, true)
                } else {
                    layoutRefresh(it, false)
                }
            }
        }
    }

    private fun getMessageContentHeight(): Int {
        var totalHeight = 0
        binding.rcvMessage.adapter?.let {
            for (i in 0 until it.itemCount) {
                val itemView: View? = binding.rcvMessage.layoutManager?.findViewByPosition(i)
                itemView?.let { iv ->
                    iv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += iv.measuredHeight
                }
            }
        }
        return totalHeight
    }

    private val handler = Handler(Looper.getMainLooper())
    private var keyboardShowing = false
    private fun layoutRefresh(bottomHeight: Int, keyboardShow: Boolean) {
        if (keyboardShow) {
            binding.llBottomPanel.visibility = View.GONE
            moveLayout(bottomHeight)
        } else {
            if (keyboardShowing) {
                IMKeyboardUtils.hideSoftInput(binding.etMessage, object : ResultReceiver(handler) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                        super.onReceiveResult(resultCode, resultData)
                        moveLayout(bottomHeight, false)
                    }
                })
            } else {
                binding.llBottomPanel.visibility = View.VISIBLE
                moveLayout(bottomHeight)
            }
        }
        keyboardShowing = keyboardShow
        if (keyboardShowing) {
            binding.ivEmo.isSelected = false
        }
    }

    private fun moveLayout(bottomHeight: Int, closeBottomPanel: Boolean = true) {
        val animators = AnimatorSet()
        val msgAnimator = ValueAnimator.ofInt(binding.rcvMessage.paddingTop, bottomHeight)
        msgAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Int
            binding.rcvMessage.setPadding(0, animatedValue, 0, 0)
        }
        msgAnimator.setTarget(binding.rcvMessage)
        msgAnimator.duration = 150
        val animator = ObjectAnimator.ofFloat(
            binding.llAlwaysShow,
            "translationY",
            (0 - bottomHeight).toFloat()
        )
        animator.duration = 150
        if (closeBottomPanel) {
            val lp = binding.llBottomPanel.layoutParams
            lp.height = bottomHeight
            binding.llBottomPanel.layoutParams = lp
            val bottomAnimator = ObjectAnimator.ofFloat(
                binding.llBottomPanel,
                "translationY",
                (0 - bottomHeight).toFloat()
            )
            bottomAnimator.duration = 150
            animators.play(msgAnimator).with(animator).with(bottomAnimator)
        } else {
            animators.play(msgAnimator).with(animator)
        }
        animators.start()
    }

    private fun initMessageRecyclerView(view: View) {
        binding.rcvMessage.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        msgAdapter = MessageAdapter(session, this, binding.rcvMessage)
        binding.rcvMessage.adapter = msgAdapter

        binding.rcvMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //当前状态为停止滑动
                    if (!recyclerView.canScrollVertically(-1)) {
                        loadMessages()
                    }
                    hasScrollToBottom = !recyclerView.canScrollVertically(1)
                    LLog.v("hasScrollToBottom :$hasScrollToBottom")
                }
            }
        })

        val linearLayoutManager =
            (binding.rcvMessage.layoutManager as LinearLayoutManager)
        linearLayoutManager.stackFromEnd = true
    }

    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.MsgNew.value, Observer<Message> {
            it?.let {
                if (it.sid == sid) {
                    val pos = msgAdapter.insertNew(it)
                    if (pos == 0) {
                        scrollToLatestMsg()
                    }
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgUpdate.value, Observer<Message> {
            it?.let {
                if (it.sid == sid) {
                    msgAdapter.update(it)
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgDelete.value, Observer<Message> {
            it?.let {
                if (it.sid == sid) {
                    msgAdapter.delete(it)
                }
            }
        })
    }

    private var cTime: Long = 0L  // 根据创建时间加载消息
    private var hasMore: Boolean = true // 是否有更多消息
    private var count = 20        // 每次加载消息的数量
    private var isLoading = false // 是否正在加载中
    private var hasScrollToBottom = false
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
        IMCoreManager.getMessageModule().queryLocalMessages(sid, cTime, count)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        composite.add(subscriber)
    }

    private fun scrollToLatestMsg(smooth: Boolean = false) {
        if (smooth) {
            binding.rcvMessage.smoothScrollToPosition(0)
        } else {
            binding.rcvMessage.scrollToPosition(0)
        }
    }

    override fun emojiOnItemClick(emoji: String) {
        val startIndex: Int = binding.etMessage.selectionStart
        val edit: Editable = binding.etMessage.editableText
        if (startIndex < 0 || startIndex >= edit.length) {
            edit.append(emoji)
        } else {
            edit.insert(startIndex, emoji)
        }
        Selection.setSelection(binding.etMessage.text, binding.etMessage.text?.length ?: 0)
    }

    override fun deleteEmoji() {
        var msgContent: String = binding.etMessage.text.toString()
        if (msgContent.isEmpty()) {
            return
        }

        if (msgContent.contains("[")) {
            msgContent = msgContent.substring(0, msgContent.lastIndexOf("["))
        }
        binding.etMessage.setText(msgContent)
        // 设置光标到末尾
        Selection.setSelection(binding.etMessage.text, binding.etMessage.text?.length ?: 0)
    }
}