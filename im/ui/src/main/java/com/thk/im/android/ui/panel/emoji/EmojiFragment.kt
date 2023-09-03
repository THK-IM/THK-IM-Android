package com.thk.im.android.ui.panel.emoji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.thk.im.android.ui.databinding.FragmentEmojiBinding
import com.thk.im.android.ui.emoji.EmojiManager

class EmojiFragment(val emojiPanelCallback: EmojiPanelCallback?) : Fragment() {

    private lateinit var emoImageAdapter: EmoImageAdapter

    /**
     * 最近表情列表
     */
    private var recentEmojiIndexList: MutableList<Int> = ArrayList()

    private var _binding: FragmentEmojiBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmojiBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEmojiRecyclerView()
        initData()
    }


    private fun initData() {
        //获取本地保存的最近表情数据
        recentEmojiIndexList = EmojiManager.getRecentEmojiIndexes()
        if (recentEmojiIndexList.size > 0) {
            val objectsRecent = EmojiManager.getEmojiList()
            emoImageAdapter.setList(objectsRecent)
        } else {
            val objects = EmojiManager.getAllEmoList()
            emoImageAdapter.setList(objects)
        }
    }

    fun updateData() {
        recentEmojiIndexList = EmojiManager.getRecentEmojiIndexes()
        if (recentEmojiIndexList.size > 0) {
            val objectsRecent = EmojiManager.getEmojiList()
            emoImageAdapter.setList(objectsRecent)
        } else {
            val objects = EmojiManager.getAllEmoList()
            emoImageAdapter.setList(objects)
        }
        emoImageAdapter.notifyDataSetChanged()
    }

    private fun initEmojiRecyclerView() {
        binding.ivDeleteEmo.setOnClickListener(View.OnClickListener {
            emojiPanelCallback?.deleteEmoji()
        })
        emoImageAdapter = EmoImageAdapter(requireContext())
        val gridLayoutManager = GridLayoutManager(context, 8)
        // 设置标题也能显示一行的数据
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (emoImageAdapter.getItemViewType(position)) {
                    EmoImageAdapter.TYPE_TITLE -> 8
                    EmoImageAdapter.TYPE_EMO -> 1
                    else -> -1
                }
            }
        }
        binding.rcv.layoutManager = gridLayoutManager
        binding.rcv.adapter = emoImageAdapter
        emoImageAdapter.setOnItemClickListener(object : EmoImageAdapter.OnItemClickListener {
            override fun onItemClick(emojiResId: Int, position: Int) {
                val emojiIndex = EmojiManager.parser.getIndexFromResId(emojiResId)
                EmojiManager.addEmojiToRecent(emojiIndex)
                val emoji = EmojiManager.parser.getTagFromResId(emojiResId)
                emojiPanelCallback?.emojiOnItemClick(emoji)
            }
        })
    }
}