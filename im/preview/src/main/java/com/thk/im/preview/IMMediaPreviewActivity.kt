package com.thk.im.preview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.preview.databinding.ActivityMediaPreviewBinding
import com.thk.im.preview.adapter.MessagePreviewAdapter
import com.thk.im.preview.view.VideoPlayerView
import com.thk.im.preview.view.ZoomableImageView
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.abs

class IMMediaPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPreviewBinding
    private lateinit var adapter: MessagePreviewAdapter
    private val compositeDisposable = CompositeDisposable()
    private var originRect = Rect(0, 0, 0, 0)
    private var previewerScrolling = false
    private var defaultId = 0L
    private var dragStartX = 0
    private var dragStartY = 0
    private val animationDuration = 300L
    private val currentPreviewView: View?
        get() {
            val recyclerView = binding.vpMediaPreview.getChildAt(0) as RecyclerView
            return recyclerView.layoutManager!!.findViewByPosition(binding.vpMediaPreview.currentItem)
        }

    private fun canChildScroll(view: View?): Boolean {
        if (view == null) {
            return false
        }
        if (view is VideoPlayerView || view is ZoomableImageView) {
            val can =
                view.canScrollHorizontally(1) ||
                        view.canScrollHorizontally(-1) ||
                        view.canScrollVertically(-1) ||
                        view.canScrollVertically(1)
            if (can) {
                return true
            }
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val can = canChildScroll(view.getChildAt(i))
                if (can) {
                    return true
                }
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defaultId = intent.getLongExtra("defaultId", 0)
        originRect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("origin_rect", Rect::class.java)!!
        } else {
            intent.getParcelableExtra<Rect>("origin_rect")!!
        }
        binding = ActivityMediaPreviewBinding.inflate(layoutInflater)
        val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("messages", Message::class.java)
        } else {
            intent.getParcelableArrayListExtra("messages")
        }
        setContentView(binding.root)
        startEnterAnimation()
        items?.let {
            adapter = MessagePreviewAdapter(this, it)
        }
        initView()
        initEventBus()
    }

    private fun initView() {
        binding.vpMediaPreview.adapter = adapter
        binding.vpMediaPreview.offscreenPageLimit = 1
        binding.vpMediaPreview.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.vpMediaPreview.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                previewerScrolling = state != ViewPager2.SCROLL_STATE_IDLE
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val recyclerView = binding.vpMediaPreview.getChildAt(0) as RecyclerView
                adapter.onPageSelected(position, recyclerView)
                onPreviewPageSelected(position)
            }
        })

        binding.vpMediaPreview.setCurrentItem(findPosition(defaultId), false)

    }

    private fun findPosition(id: Long): Int {
        val messages = adapter.getMessages()
        for (i in messages.indices) {
            if (messages[i].id == id) {
                return i
            }
        }
        return 0
    }

    private fun initEventBus() {
        XEventBus.observe(this, IMEvent.MsgUpdate.value, Observer<Message> {
            it?.let {
                if (it.type == MsgType.IMAGE.value) {
                    val adapter = binding.vpMediaPreview.adapter as MessagePreviewAdapter
                    adapter.updateMessage(it)
                }
            }
        })
        XEventBus.observe(this, IMEvent.MsgNew.value, Observer<Message> {
            it?.let {
                if (it.type == MsgType.IMAGE.value) {
                    val adapter = binding.vpMediaPreview.adapter as MessagePreviewAdapter
                    adapter.updateMessage(it)
                }
            }
        })
    }

    private fun intercept(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStartX = event.rawX.toInt()
                dragStartY = event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                val transitionX = event.rawX - dragStartX
                val transitionY = event.rawY - dragStartY
                if (abs(transitionX) < abs(transitionY) || binding.clContent.alpha != 1f) {
                    translatePreview(transitionX, transitionY)
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (binding.clContent.background.alpha != 255) {
                    if (binding.clContent.background.alpha < 180) {
                        binding.clContent.background.alpha = 0
                        this@IMMediaPreviewActivity.exit()
                    } else {
                        reset()
                    }
                }
            }
        }
    }

    private fun translatePreview(transitionX: Float, transitionY: Float) {
        val alpha = 1 - abs(transitionY) / com.thk.im.android.core.base.utils.AppUtils.instance().screenHeight
        val scale = maxOf(minOf(1f, alpha), 0.7f)
        binding.vpMediaPreview.translationX = transitionX / binding.clContent.scaleX
        binding.vpMediaPreview.translationY = transitionY / binding.clContent.scaleX
        binding.vpMediaPreview.scaleX = scale
        binding.vpMediaPreview.scaleY = scale
        binding.clContent.background.alpha = (alpha * 255).toInt()
        val recyclerView = binding.vpMediaPreview.getChildAt(0) as RecyclerView
        adapter.hideChildren(binding.vpMediaPreview.currentItem, recyclerView)
    }

    private fun reset() {
        val recyclerView = binding.vpMediaPreview.getChildAt(0) as RecyclerView
        adapter.showChildren(binding.vpMediaPreview.currentItem, recyclerView)
        binding.vpMediaPreview.translationX = 0f
        binding.vpMediaPreview.translationY = 0f
        binding.vpMediaPreview.scaleX = 1f
        binding.vpMediaPreview.scaleY = 1f
        binding.clContent.background.alpha = 255
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (binding.clContent.background.alpha != 255) {
            intercept(ev)
            return true
        }
        val canChildScrolled = canChildScroll(currentPreviewView)
        if (!canChildScrolled && !previewerScrolling) {
            intercept(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun startEnterAnimation() {
        val scaleStart = originRect.width().toFloat() / com.thk.im.android.core.base.utils.AppUtils.instance().screenWidth
        val startLocation = floatArrayOf(
            (originRect.left).toFloat() + (originRect.width() / 2) - com.thk.im.android.core.base.utils.AppUtils.instance().screenWidth / 2,
            (originRect.top).toFloat() + (originRect.height() / 2) - com.thk.im.android.core.base.utils.AppUtils.instance().screenHeight / 2,
        )
        val location = floatArrayOf(0f, 0f)

        val translationX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "translationX", startLocation[0], location[0]
        )
        val translationY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "translationY", startLocation[1], location[1]
        )
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            binding.vpMediaPreview, translationX, translationY
        )
        animator.interpolator = LinearInterpolator()

        val scaleX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "scaleX", scaleStart, 1f
        )
        val scaleY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "scaleY", scaleStart, 1f
        )
        val scaleAnimation =
            ObjectAnimator.ofPropertyValuesHolder(binding.vpMediaPreview, scaleX, scaleY)
        scaleAnimation.interpolator = LinearInterpolator()

        val colorAnim = ObjectAnimator.ofInt(
            binding.clContent, "backgroundColor", Color.TRANSPARENT, Color.BLACK
        )
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.duration = animationDuration

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animator, scaleAnimation, colorAnim)

        animatorSet.duration = animationDuration
        animatorSet.start()
    }

    private fun exit() {
        startExitAnimation()
    }

    private fun startExitAnimation() {
        val defaultPosition = findPosition(defaultId)
        if (binding.vpMediaPreview.currentItem != defaultPosition) {
            finish()
        } else {
            val scaleStart = originRect.width().toFloat() / com.thk.im.android.core.base.utils.AppUtils.instance().screenWidth
            val startLocation = floatArrayOf(
                (originRect.left).toFloat() + (originRect.width() / 2) - com.thk.im.android.core.base.utils.AppUtils.instance().screenWidth / 2,
                (originRect.top).toFloat() + (originRect.height() / 2) - com.thk.im.android.core.base.utils.AppUtils.instance().screenHeight / 2,
            )
            val translationX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
                "translationX",
                binding.vpMediaPreview.translationX, startLocation[0],
            )
            val translationY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
                "translationY",
                binding.vpMediaPreview.translationY, startLocation[1],
            )
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                binding.vpMediaPreview, translationX, translationY
            )
            animator.interpolator = LinearInterpolator()
            val scaleX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
                "scaleX", binding.vpMediaPreview.scaleX, scaleStart
            )
            val scaleY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
                "scaleY", binding.vpMediaPreview.scaleY, scaleStart
            )
            val scaleAnimation = ObjectAnimator.ofPropertyValuesHolder(
                binding.vpMediaPreview, scaleX, scaleY
            )
            scaleAnimation.interpolator = LinearInterpolator()

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animator, scaleAnimation)
            animatorSet.duration = animationDuration
            animatorSet.start()
            animatorSet.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    finish()
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }

            })
        }
    }

    private fun onPreviewPageSelected(position: Int) {
        val adapter = binding.vpMediaPreview.adapter as MessagePreviewAdapter
        if (position != 0 && position != adapter.itemCount - 1) {
            return
        }
        val message = adapter.getMessage(position)
        message?.let {
            if (position == 0) {
                loadMessages(it, true)
            } else if (position == adapter.itemCount - 1) {
                loadMessages(it, false)
            }
        }
    }

    private fun loadMessages(message: Message, older: Boolean) {
        val subscriber = object : BaseSubscriber<List<Message>>() {
            override fun onNext(t: List<Message>?) {
                t?.let {
                    val adapter = binding.vpMediaPreview.adapter as MessagePreviewAdapter
                    adapter.addOlderMessage(t, older)
                }
            }
        }
        Flowable.just(message)
            .map {
                val messages = when (older) {
                    true -> {
                        IMCoreManager.getImDataBase().messageDao().findOlderMessage(
                            it.sid,
                            it.msgId,
                            arrayOf(MsgType.IMAGE.value, MsgType.VIDEO.value),
                            it.cTime,
                            10
                        ).reversed()
                    }

                    else -> {
                        IMCoreManager.getImDataBase().messageDao().findNewerMessage(
                            it.sid,
                            it.msgId,
                            arrayOf(MsgType.IMAGE.value, MsgType.VIDEO.value),
                            it.cTime,
                            10
                        )
                    }
                }
                return@map messages
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        compositeDisposable.add(subscriber)
    }

    override fun onBackPressed() {
        exit()
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}