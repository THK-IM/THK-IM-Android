package com.thk.im.preview.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import java.util.LinkedList
import java.util.Queue


class ZoomableImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val animationDuration = 200

    private val factor = 0.9f
    private val modeFree = 0
    private val modeScroll = 1
    private val modeScale = 2

    private val maxScale = 3f

    private val mOuterMatrix = Matrix()

    private val mLastMovePoint = PointF()
    private val mScaleCenter = PointF()

    private var mOnClickListener: OnClickListener? = null

    private var mOnLongClickListener: OnLongClickListener? = null
    private var mMask: RectF? = null

    private var currentMode = modeFree

    private var mScaleBase = 0f
    private var mScaleAnimator: ScaleAnimator? = null
    private var mFlingAnimator: FlingAnimator? = null
    private val mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (currentMode == modeFree && !(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                fling(velocityX, velocityY)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            //触发长按
            if (mOnLongClickListener != null) {
                mOnLongClickListener!!.onLongClick(this@ZoomableImageView)
            }
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            //当手指快速第二次按下触发,此时必须是单指模式才允许执行doubleTap
            if (currentMode == modeScroll && !(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                doubleTap(e.x, e.y)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            //触发点击
            if (mOnClickListener != null) {
                mOnClickListener!!.onClick(this@ZoomableImageView)
            }
            return true
        }
    })

    init {
        initView()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mOnClickListener = l
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        mOnLongClickListener = l
    }

    private fun getInnerMatrix(mt: Matrix?): Matrix {
        var matrix = mt
        if (matrix == null) {
            matrix = Matrix()
        } else {
            matrix.reset()
        }
        if (isReady()) {
            //原图大小
            val tempSrc = MathUtils.rectFTake(
                0f,
                0f,
                drawable.intrinsicWidth.toFloat(),
                drawable.intrinsicHeight.toFloat()
            )
            //控件大小
            val tempDst = MathUtils.rectFTake(0f, 0f, width.toFloat(), height.toFloat())
            //计算fit center矩阵
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
            //释放临时对象
            MathUtils.rectFGiven(tempDst)
            MathUtils.rectFGiven(tempSrc)
        }
        return matrix
    }

    private fun getCurrentImageMatrix(mt: Matrix): Matrix {
        //获取内部变换矩阵
        var matrix = mt
        matrix = getInnerMatrix(matrix)
        //乘上外部变换矩阵
        matrix.postConcat(mOuterMatrix)
        return matrix
    }

    private fun getImageBound(rf: RectF?): RectF {
        var rectF = rf
        if (rectF == null) {
            rectF = RectF()
        } else {
            rectF.setEmpty()
        }
        return if (!isReady()) {
            rectF
        } else {
            //申请一个空matrix
            val matrix = MathUtils.matrixTake()
            //获取当前总变换矩阵
            getCurrentImageMatrix(matrix)
            //对原图矩形进行变换得到当前显示矩形
            rectF[0f, 0f, drawable.intrinsicWidth.toFloat()] = drawable.intrinsicHeight.toFloat()
            matrix.mapRect(rectF)
            //释放临时matrix
            MathUtils.matrixGiven(matrix)
            rectF
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        if (currentMode == modeScale) {
            return true
        }
        val bound = getImageBound(null)
        if (bound.isEmpty) {
            return false
        }
        return if (direction > 0) {
            bound.right > width
        } else {
            bound.left < 0
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        if (currentMode == modeScale) {
            return true
        }
        val bound = getImageBound(null)
        if (bound.isEmpty) {
            return false
        }
        return if (direction > 0) {
            bound.bottom > height
        } else {
            bound.top < 0
        }
    }

    fun reset() {
        //重置位置到fit
        mOuterMatrix.reset()
        //清空mask
        mMask = null
        //停止所有手势
        currentMode = modeFree
        mLastMovePoint[0f] = 0f
        mScaleCenter[0f] = 0f
        mScaleBase = 0f
        cancelAllAnimator()
        //重绘
        invalidate()
    }


    private fun getMaxScale(): Float {
        return maxScale
    }

    private fun calculateNextScale(innerScale: Float, outerScale: Float): Float {
        val currentScale = innerScale * outerScale
        return if (currentScale < maxScale) {
            maxScale
        } else {
            innerScale
        }
    }

    private fun initView() {
        super.setScaleType(ScaleType.MATRIX)
    }

    //不允许设置scaleType，只能用内部设置的matrix
    override fun setScaleType(scaleType: ScaleType?) {}

    override fun onDraw(canvas: Canvas) {
        if (isReady()) {
            val matrix = MathUtils.matrixTake()
            imageMatrix = getCurrentImageMatrix(matrix)
            MathUtils.matrixGiven(matrix)
        }
        //对图像做遮罩处理
        if (mMask != null) {
            canvas.save()
            canvas.clipRect(mMask!!)
            super.onDraw(canvas)
            canvas.restore()
        } else {
            super.onDraw(canvas)
        }
    }

    private fun isReady(): Boolean {
        return drawable != null &&
                drawable!!.intrinsicWidth > 0 &&
                drawable!!.intrinsicHeight > 0 &&
                width > 0 && height > 0
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val action = event.action and MotionEvent.ACTION_MASK
        //最后一个点抬起或者取消，结束所有模式
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            //如果之前是缩放模式,还需要触发一下缩放结束动画
            if (currentMode == modeScale) {
                scaleEnd()
            }
            currentMode = modeFree
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            //多个手指情况下抬起一个手指,此时需要是缩放模式才触发
            if (currentMode == modeScale) {
                //抬起的点如果大于2，那么缩放模式还有效，但是有可能初始点变了，重新测量初始点
                if (event.pointerCount > 2) {
                    //如果还没结束缩放模式，但是第一个点抬起了，那么让第二个点和第三个点作为缩放控制点
                    if (event.action shr 8 == 0) {
                        saveScaleContext(event.getX(1), event.getY(1), event.getX(2), event.getY(2))
                        //如果还没结束缩放模式，但是第二个点抬起了，那么让第一个点和第三个点作为缩放控制点
                    } else if (event.action shr 8 == 1) {
                        saveScaleContext(event.getX(0), event.getY(0), event.getX(2), event.getY(2))
                    }
                }
                //如果抬起的点等于2,那么此时只剩下一个点,也不允许进入单指模式,因为此时可能图片没有在正确的位置上
            }
            //第一个点按下，开启滚动模式，记录开始滚动的点
        } else if (action == MotionEvent.ACTION_DOWN) {
            //在矩阵动画过程中不允许启动滚动模式
            if (!(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                //停止所有动画
                cancelAllAnimator()
                //切换到滚动模式
                currentMode = modeScroll
                //保存触发点用于move计算差值
                mLastMovePoint[event.x] = event.y
            }
            //非第一个点按下，关闭滚动模式，开启缩放模式，记录缩放模式的一些初始数据
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            //停止所有动画
            cancelAllAnimator()
            //切换到缩放模式
            currentMode = modeScale
            //保存缩放的两个手指
            saveScaleContext(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!(mScaleAnimator != null && mScaleAnimator!!.isRunning)) {
                //在滚动模式下移动
                if (currentMode == modeScroll) {
                    //每次移动产生一个差值累积到图片位置上
                    scrollBy(event.x - mLastMovePoint.x, event.y - mLastMovePoint.y)
                    //记录新的移动点
                    mLastMovePoint[event.x] = event.y
                    //在缩放模式下移动
                } else if (currentMode == modeScale && event.pointerCount > 1) {
                    //两个缩放点间的距离
                    val distance = MathUtils.getDistance(
                        event.getX(0),
                        event.getY(0),
                        event.getX(1),
                        event.getY(1)
                    )
                    //保存缩放点中点
                    val lineCenter = MathUtils.getCenterPoint(
                        event.getX(0),
                        event.getY(0),
                        event.getX(1),
                        event.getY(1)
                    )
                    mLastMovePoint[lineCenter[0]] = lineCenter[1]
                    //处理缩放
                    scale(mScaleCenter, mScaleBase, distance, mLastMovePoint)
                }
            }
        }
        //无论如何都处理各种外部手势
        mGestureDetector.onTouchEvent(event)
        return true
    }

    private fun scrollBy(x: Float, y: Float): Boolean {
        var xDiff = x
        var yDiff = y
        if (!isReady()) {
            return false
        }
        //原图方框
        val bound = MathUtils.rectFTake()
        getImageBound(bound)
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //如果当前图片宽度小于控件宽度，则不能移动
        if (bound.right - bound.left < displayWidth) {
            xDiff = 0f
            //如果图片左边在移动后超出控件左边
        } else if (bound.left + xDiff > 0) {
            //如果在移动之前是没超出的，计算应该移动的距离
            xDiff = if (bound.left < 0) {
                -bound.left
                //否则无法移动
            } else {
                0f
            }
            //如果图片右边在移动后超出控件右边
        } else if (bound.right + xDiff < displayWidth) {
            //如果在移动之前是没超出的，计算应该移动的距离
            xDiff = if (bound.right > displayWidth) {
                displayWidth - bound.right
                //否则无法移动
            } else {
                0f
            }
        }
        //以下同理
        if (bound.bottom - bound.top < displayHeight) {
            yDiff = 0f
        } else if (bound.top + yDiff > 0) {
            yDiff = if (bound.top < 0) {
                -bound.top
            } else {
                0f
            }
        } else if (bound.bottom + yDiff < displayHeight) {
            yDiff = if (bound.bottom > displayHeight) {
                displayHeight - bound.bottom
            } else {
                0f
            }
        }
        MathUtils.rectFGiven(bound)
        //应用移动变换
        mOuterMatrix.postTranslate(xDiff, yDiff)
        //触发重绘
        invalidate()
        //检查是否有变化
        return xDiff != 0f || yDiff != 0f
    }

    private fun saveScaleContext(x1: Float, y1: Float, x2: Float, y2: Float) {
        //记录基础缩放值,其中图片缩放比例按照x方向来计算
        //理论上图片应该是等比的,x和y方向比例相同
        //但是有可能外部设定了不规范的值.
        //但是后续的scale操作会将xy不等的缩放值纠正,改成和x方向相同
        mScaleBase =
            MathUtils.getMatrixScale(mOuterMatrix)[0] / MathUtils.getDistance(x1, y1, x2, y2)
        //两手指的中点在屏幕上落在了图片的某个点上,图片上的这个点在经过总矩阵变换后和手指中点相同
        //现在我们需要得到图片上这个点在图片是fit center状态下在屏幕上的位置
        //因为后续的计算都是基于图片是fit center状态下进行变换
        //所以需要把两手指中点除以外层变换矩阵得到mScaleCenter
        val center =
            MathUtils.inverseMatrixPoint(MathUtils.getCenterPoint(x1, y1, x2, y2), mOuterMatrix)
        mScaleCenter[center[0]] = center[1]
    }

    private fun scale(scaleCenter: PointF, scaleBase: Float, distance: Float, lineCenter: PointF) {
        if (!isReady()) {
            return
        }
        //计算图片从fit center状态到目标状态的缩放比例
        val scale = scaleBase * distance
        val matrix = MathUtils.matrixTake()
        //按照图片缩放中心缩放，并且让缩放中心在缩放点中点上
        matrix.postScale(scale, scale, scaleCenter.x, scaleCenter.y)
        //让图片的缩放中点跟随手指缩放中点
        matrix.postTranslate(lineCenter.x - scaleCenter.x, lineCenter.y - scaleCenter.y)
        //应用变换
        mOuterMatrix.set(matrix)
        MathUtils.matrixGiven(matrix)
        //重绘
        invalidate()
    }

    /**
     * 双击后放大或者缩小
     *
     *
     * 将图片缩放比例缩放到nextScale指定的值.
     * 但nextScale值不能大于最大缩放值不能小于fit center情况下的缩放值.
     * 将双击的点尽量移动到控件中心.
     *
     * @param x 双击的点
     * @param y 双击的点
     * @see .calculateNextScale
     * @see .getMaxScale
     */
    private fun doubleTap(x: Float, y: Float) {
        if (!isReady()) {
            return
        }
        //获取第一层变换矩阵
        val innerMatrix = MathUtils.matrixTake()
        getInnerMatrix(innerMatrix)
        //当前总的缩放比例
        val innerScale = MathUtils.getMatrixScale(innerMatrix)[0]
        val outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0]
        val currentScale = innerScale * outerScale
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //最大放大大小
        val maxScale = getMaxScale()
        //接下来要放大的大小
        var nextScale = calculateNextScale(innerScale, outerScale)
        //如果接下来放大大于最大值或者小于fit center值，则取边界
        if (nextScale > maxScale) {
            nextScale = maxScale
        }
        if (nextScale < innerScale) {
            nextScale = innerScale
        }
        //开始计算缩放动画的结果矩阵
        val animEnd = MathUtils.matrixTake(mOuterMatrix)
        //计算还需缩放的倍数
        animEnd.postScale(nextScale / currentScale, nextScale / currentScale, x, y)
        //将放大点移动到控件中心
        animEnd.postTranslate(displayWidth / 2f - x, displayHeight / 2f - y)
        //得到放大之后的图片方框
        val testMatrix = MathUtils.matrixTake(innerMatrix)
        testMatrix.postConcat(animEnd)
        val testBound = MathUtils.rectFTake(
            0f,
            0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        testMatrix.mapRect(testBound)
        //修正位置
        var postX = 0f
        var postY = 0f
        if (testBound.right - testBound.left < displayWidth) {
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f
        } else if (testBound.left > 0) {
            postX = -testBound.left
        } else if (testBound.right < displayWidth) {
            postX = displayWidth - testBound.right
        }
        if (testBound.bottom - testBound.top < displayHeight) {
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
        } else if (testBound.top > 0) {
            postY = -testBound.top
        } else if (testBound.bottom < displayHeight) {
            postY = displayHeight - testBound.bottom
        }
        //应用修正位置
        animEnd.postTranslate(postX, postY)
        //清理当前可能正在执行的动画
        cancelAllAnimator()
        //启动矩阵动画
        mScaleAnimator = ScaleAnimator(mOuterMatrix, animEnd)
        mScaleAnimator!!.start()
        //清理临时变量
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(animEnd)
        MathUtils.matrixGiven(innerMatrix)
    }

    /**
     * 当缩放操作结束动画
     *
     *
     * 如果图片超过边界,找到最近的位置动画恢复.
     * 如果图片缩放尺寸超过最大值或者最小值,找到最近的值动画恢复.
     */
    private fun scaleEnd() {
        if (!isReady()) {
            return
        }
        //是否修正了位置
        var change = false
        //获取图片整体的变换矩阵
        val currentMatrix = MathUtils.matrixTake()
        getCurrentImageMatrix(currentMatrix)
        //整体缩放比例
        val currentScale = MathUtils.getMatrixScale(currentMatrix)[0]
        //第二层缩放比例
        val outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0]
        //控件大小
        val displayWidth = width.toFloat()
        val displayHeight = height.toFloat()
        //最大缩放比例
        val maxScale = getMaxScale()
        //比例修正
        var scalePost = 1f
        //位置修正
        var postX = 0f
        var postY = 0f
        //如果整体缩放比例大于最大比例，进行缩放修正
        if (currentScale > maxScale) {
            scalePost = maxScale / currentScale
        }
        //如果缩放修正后整体导致第二层缩放小于1（就是图片比fit center状态还小），重新修正缩放
        if (outerScale * scalePost < 1f) {
            scalePost = 1f / outerScale
        }
        //如果缩放修正不为1，说明进行了修正
        if (scalePost != 1f) {
            change = true
        }
        //尝试根据缩放点进行缩放修正
        val testMatrix = MathUtils.matrixTake(currentMatrix)
        testMatrix.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
        val testBound = MathUtils.rectFTake(
            0f,
            0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        //获取缩放修正后的图片方框
        testMatrix.mapRect(testBound)
        //检测缩放修正后位置有无超出，如果超出进行位置修正
        if (testBound.right - testBound.left < displayWidth) {
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f
        } else if (testBound.left > 0) {
            postX = -testBound.left
        } else if (testBound.right < displayWidth) {
            postX = displayWidth - testBound.right
        }
        if (testBound.bottom - testBound.top < displayHeight) {
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f
        } else if (testBound.top > 0) {
            postY = -testBound.top
        } else if (testBound.bottom < displayHeight) {
            postY = displayHeight - testBound.bottom
        }
        //如果位置修正不为0，说明进行了修正
        if (postX != 0f || postY != 0f) {
            change = true
        }
        //只有有执行修正才执行动画
        if (change) {
            //计算结束矩阵
            val animEnd = MathUtils.matrixTake(mOuterMatrix)
            animEnd.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y)
            animEnd.postTranslate(postX, postY)
            //清理当前可能正在执行的动画
            cancelAllAnimator()
            //启动矩阵动画
            mScaleAnimator = ScaleAnimator(mOuterMatrix, animEnd)
            mScaleAnimator!!.start()
            //清理临时变量
            MathUtils.matrixGiven(animEnd)
        }
        //清理临时变量
        MathUtils.rectFGiven(testBound)
        MathUtils.matrixGiven(testMatrix)
        MathUtils.matrixGiven(currentMatrix)
    }

    private fun fling(vx: Float, vy: Float) {
        if (!isReady()) {
            return
        }
        //清理当前可能正在执行的动画
        cancelAllAnimator()
        //创建惯性动画
        //FlingAnimator单位为 像素/帧,一秒60帧
        mFlingAnimator = FlingAnimator(vx / 60f, vy / 60f)
        mFlingAnimator!!.start()
    }

    private fun cancelAllAnimator() {
        if (mScaleAnimator != null) {
            mScaleAnimator!!.cancel()
            mScaleAnimator = null
        }
        if (mFlingAnimator != null) {
            mFlingAnimator!!.cancel()
            mFlingAnimator = null
        }
    }

    /**
     * 对象池
     *
     *
     * 防止频繁new对象产生内存抖动.
     * 由于对象池最大长度限制,如果吞度量超过对象池容量,仍然会发生抖动.
     * 此时需要增大对象池容量,但是会占用更多内存.
     *
     * @param <T> 对象池容纳的对象类型
    </T> */
    private abstract class ObjectsPool<T>(
        /**
         * 对象池的最大容量
         */
        private val mSize: Int
    ) {
        /**
         * 对象池队列
         */
        private val mQueue: Queue<T>

        init {
            mQueue = LinkedList()
        }

        fun take(): T {
            //如果池内为空就创建一个
            return if (mQueue.size == 0) {
                newInstance()
            } else {
                //对象池里有就从顶端拿出来一个返回
                resetInstance(mQueue.poll()!!)
            }
        }

        /**
         * 归还对象池内申请的对象
         *
         *
         * 如果归还的对象数量超过对象池容量,那么归还的对象就会被丢弃.
         *
         * @param obj 归还的对象
         * @see .take
         */
        fun given(obj: T?) {
            //如果对象池还有空位子就归还对象
            if (obj != null && mQueue.size < mSize) {
                mQueue.offer(obj)
            }
        }

        /**
         * 实例化对象
         *
         * @return 创建的对象
         */
        protected abstract fun newInstance(): T

        /**
         * 重置对象
         *
         *
         * 把对象数据清空到就像刚创建的一样.
         *
         * @param obj 需要被重置的对象
         * @return 被重置之后的对象
         */
        protected abstract fun resetInstance(obj: T): T
    }

    /**
     * 矩阵对象池
     */
    private class MatrixPool(size: Int) : ObjectsPool<Matrix>(size) {
        override fun newInstance(): Matrix {
            return Matrix()
        }

        override fun resetInstance(obj: Matrix): Matrix {
            obj.reset()
            return obj
        }
    }

    /**
     * 矩形对象池
     */
    private class RectFPool(size: Int) : ObjectsPool<RectF>(size) {
        override fun newInstance(): RectF {
            return RectF()
        }

        override fun resetInstance(obj: RectF): RectF {
            obj.setEmpty()
            return obj
        }
    }


    ////////////////////////////////防止内存抖动复用对象////////////////////////////////

    ////////////////////////////////防止内存抖动复用对象////////////////////////////////
    /**
     * 数学计算工具类
     */
    object MathUtils {
        /**
         * 矩阵对象池
         */
        private val mMatrixPool = MatrixPool(16)

        /**
         * 矩形对象池
         */
        private val mRectFPool = RectFPool(16)

        /**
         * 获取矩阵对象
         */
        fun matrixTake(): Matrix {
            return mMatrixPool.take()
        }

        /**
         * 获取某个矩阵的copy
         */
        fun matrixTake(matrix: Matrix?): Matrix {
            val result = mMatrixPool.take()
            if (matrix != null) {
                result.set(matrix)
            }
            return result
        }

        /**
         * 归还矩阵对象
         */
        fun matrixGiven(matrix: Matrix) {
            mMatrixPool.given(matrix)
        }

        /**
         * 获取矩形对象
         */
        fun rectFTake(): RectF {
            return mRectFPool.take()
        }

        /**
         * 按照指定值获取矩形对象
         */
        fun rectFTake(left: Float, top: Float, right: Float, bottom: Float): RectF {
            val result = mRectFPool.take()
            result[left, top, right] = bottom
            return result
        }

        /**
         * 归还矩形对象
         */
        fun rectFGiven(rectF: RectF) {
            mRectFPool.given(rectF)
        }

        /**
         * 获取两点之间距离
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return 距离
         */
        fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x = x1 - x2
            val y = y1 - y2
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }

        /**
         * 获取两点的中点
         *
         * @param x1 点1
         * @param y1 点1
         * @param x2 点2
         * @param y2 点2
         * @return float[]{x, y}
         */
        fun getCenterPoint(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
            return floatArrayOf((x1 + x2) / 2f, (y1 + y2) / 2f)
        }

        /**
         * 获取矩阵的缩放值
         *
         * @param matrix 要计算的矩阵
         * @return float[]{scaleX, scaleY}
         */
        fun getMatrixScale(matrix: Matrix?): FloatArray {
            return if (matrix != null) {
                val value = FloatArray(9)
                matrix.getValues(value)
                floatArrayOf(value[0], value[4])
            } else {
                FloatArray(2)
            }
        }

        /**
         * 计算点除以矩阵的值
         *
         *
         * matrix.mapPoints(unknownPoint) -> point
         * 已知point和matrix,求unknownPoint的值.
         *
         * @return unknownPoint
         */
        fun inverseMatrixPoint(point: FloatArray?, matrix: Matrix?): FloatArray {
            return if (point != null && matrix != null) {
                val dst = FloatArray(2)
                //计算matrix的逆矩阵
                val inverse = matrixTake()
                matrix.invert(inverse)
                //用逆矩阵变换point到dst,dst就是结果
                inverse.mapPoints(dst, point)
                //清除临时变量
                matrixGiven(inverse)
                dst
            } else {
                FloatArray(2)
            }
        }
    }

    private inner class FlingAnimator(vectorX: Float, vectorY: Float) :
        ValueAnimator(), AnimatorUpdateListener {

        private val mVector: FloatArray

        init {
            setFloatValues(0f, 1f)
            duration = 1000000
            addUpdateListener(this)
            mVector = floatArrayOf(vectorX, vectorY)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //移动图像并给出结果
            val result: Boolean = scrollBy(mVector[0], mVector[1])
            //衰减速度
            mVector[0] *= factor
            mVector[1] *= factor
            //速度太小或者不能移动了就结束
            if (!result || MathUtils.getDistance(0f, 0f, mVector[0], mVector[1]) < 1f) {
                animation.cancel()
            }
        }
    }

    private inner class ScaleAnimator @JvmOverloads constructor(
        start: Matrix,
        end: Matrix,
        duration: Long = animationDuration.toLong()
    ) :
        ValueAnimator(), AnimatorUpdateListener {
        private val mStart = FloatArray(9)
        private val mEnd = FloatArray(9)
        private val mResult = FloatArray(9)

        init {
            setFloatValues(0f, 1f)
            setDuration(duration)
            addUpdateListener(this)
            start.getValues(mStart)
            end.getValues(mEnd)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //获取动画进度
            val value = animation.animatedValue as Float
            //根据动画进度计算矩阵中间插值
            for (i in 0..8) {
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value
            }
            //设置矩阵并重绘
            mOuterMatrix.setValues(mResult)
            invalidate()
        }
    }
}