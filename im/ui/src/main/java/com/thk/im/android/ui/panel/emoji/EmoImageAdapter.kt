package com.thk.im.android.ui.panel.emoji

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thk.im.android.ui.R
import com.thk.im.android.ui.panel.emoji.bean.EmoBean
import com.thk.im.android.ui.panel.emoji.bean.EmoImageBean
import com.thk.im.android.ui.panel.emoji.bean.EmoTextBean


/**
 * 表情图片的adapter
 */
class EmoImageAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var list: MutableList<EmoBean> = ArrayList<EmoBean>()
    fun setList(list: MutableList<EmoBean>) {
        this.list = list
    }

    /**
     * 添加最近使用的表情
     *
     * @param emoList
     */
    fun setRecentlyList(emoList: List<EmoBean>?) {
        if (emoList.isNullOrEmpty()) {
            return
        }
        list.clear()
        list.addAll(emoList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is EmoTextBean) {
            TYPE_TITLE //标题
        } else if (list[position] is EmoImageBean) {
            TYPE_EMO //表情
        } else {
            -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder = if (viewType == TYPE_TITLE) {
            val inflate: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emo_title, parent, false)
            TileViewHolder(inflate)
        } else {
            val inflate: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emo_image, parent, false)
            EmoImageHolder(inflate)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TileViewHolder) {
            holder.initData(position)
        } else if (holder is EmoImageHolder) {
            holder.initData(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * 标题
     */
    inner class TileViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val tv_title: TextView

        init {
            tv_title = itemView.findViewById<TextView>(R.id.tv_title)
        }

        fun initData(position: Int) {
            val bean: EmoTextBean = list[position] as EmoTextBean
            tv_title.text = bean.title
        }
    }

    /**
     * 表情
     */
    inner class EmoImageHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val iv_emo: ImageView

        init {
            iv_emo = itemView.findViewById<ImageView>(R.id.iv_emo)
        }

        fun initData(position: Int) {
            val emoImageBean: EmoImageBean? = list[position] as EmoImageBean?
            if (emoImageBean != null) {
                val emoId: Int = emoImageBean.emoId ?: 0
                iv_emo.setImageBitmap(getBitmap(emoId))
                itemView.setOnClickListener { mItemClickListener!!.onItemClick(emoId, position) }
            }
        }
    }

    /**
     * 把资源ID转位图片显示
     *
     * @return
     */
    private fun getBitmap(emoId: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeResource(context.resources, emoId)
        } catch (e: Exception) {
        }
        return bitmap
    }

    private var mItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(emojiResId: Int, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mItemClickListener = listener
    }

    companion object {
        const val TYPE_TITLE = 1
        const val TYPE_EMO = 2
    }
}