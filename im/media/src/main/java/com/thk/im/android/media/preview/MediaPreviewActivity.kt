package com.thk.im.android.media.preview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.thk.im.android.base.AppUtils
import com.thk.im.android.base.LLog
import com.thk.im.android.media.databinding.ActivityMediaPreviewBinding
import com.thk.im.android.media.preview.adapter.MediaPreviewAdapter
import com.thk.im.android.ui.manager.MediaItem
import kotlin.math.abs

class MediaPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPreviewBinding
    private lateinit var adapter: MediaPreviewAdapter
    private var originRect = Rect(0, 0, 0, 0)
    private var dragStartX = 0
    private var dragStartY = 0
    private val animationDuration = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originRect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("origin_rect", Rect::class.java)!!
        } else {
            intent.getParcelableExtra<Rect>("origin_rect")!!
        }
        binding = ActivityMediaPreviewBinding.inflate(layoutInflater)
        val items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("media_items", MediaItem::class.java)
        } else {
            intent.getParcelableArrayListExtra("media_items")
        }
        setContentView(binding.root)
        startEnterAnimation()
        items?.let {
            adapter = MediaPreviewAdapter(this, it)
        }
        initView()
    }

    private fun initView() {
        binding.vpMediaPreview.adapter = adapter
        binding.vpMediaPreview.offscreenPageLimit = 2
        binding.vpMediaPreview.registerOnPageChangeCallback(object : OnPageChangeCallback() {
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        LLog.v("dispatchTouchEvent: ${event.pointerCount}, ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStartX = event.rawX.toInt()
                dragStartY = event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1 && binding.clContent.background.alpha != 255) {
                    return super.dispatchTouchEvent(event)
                }
                val transitionX = event.rawX - dragStartX
                val transitionY = event.rawY - dragStartY
                if (abs(transitionX) < abs(transitionY) || binding.clContent.alpha != 1f) {
                    val alpha = 1 - abs(transitionY) / AppUtils.instance().screenHeight
                    val scale = maxOf(minOf(1f, alpha), 0.7f)
                    binding.vpMediaPreview.translationX = transitionX / binding.clContent.scaleX
                    binding.vpMediaPreview.translationY = transitionY / binding.clContent.scaleX
                    binding.vpMediaPreview.scaleX = scale
                    binding.vpMediaPreview.scaleY = scale
                    binding.clContent.background.alpha = (alpha * 255).toInt()
                }
                if (binding.clContent.background.alpha != 255) {
                    return false
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (binding.clContent.background.alpha != 255) {
                    if (binding.clContent.background.alpha < 180) {
                        binding.clContent.background.alpha = 0
                        exit()
                    } else {
                        binding.vpMediaPreview.translationX = 0f
                        binding.vpMediaPreview.translationY = 0f
                        binding.vpMediaPreview.scaleX = 1f
                        binding.vpMediaPreview.scaleY = 1f
                        binding.clContent.background.alpha = 255
                    }
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun startEnterAnimation() {
        val scaleStart = originRect.width().toFloat() / AppUtils.instance().screenWidth
        val startLocation = floatArrayOf(
            (originRect.left).toFloat() + (originRect.width() / 2) - AppUtils.instance().screenWidth / 2,
            (originRect.top).toFloat() + (originRect.height() / 2) - AppUtils.instance().screenHeight / 2,
        )
        val location = floatArrayOf(0f, 0f)

        val translationX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "translationX", startLocation[0], location[0]
        )
        val translationY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "translationY", startLocation[1], location[1]
        )
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            binding.vpMediaPreview,
            translationX,
            translationY
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

        val colorAnim =
            ObjectAnimator.ofInt(binding.clContent, "backgroundColor", Color.TRANSPARENT, Color.BLACK)
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
        val scaleStart = originRect.width().toFloat() / AppUtils.instance().screenWidth
        val startLocation = floatArrayOf(
            (originRect.left).toFloat() + (originRect.width() / 2) - AppUtils.instance().screenWidth / 2,
            (originRect.top).toFloat() + (originRect.height() / 2) - AppUtils.instance().screenHeight / 2,
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
            binding.vpMediaPreview,
            translationX,
            translationY
        )
        animator.interpolator = LinearInterpolator()

        val scaleX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "scaleX", binding.vpMediaPreview.scaleX, scaleStart
        )
        val scaleY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(
            "scaleY", binding.vpMediaPreview.scaleY, scaleStart
        )
        val scaleAnimation =
            ObjectAnimator.ofPropertyValuesHolder(binding.vpMediaPreview, scaleX, scaleY)
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

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}