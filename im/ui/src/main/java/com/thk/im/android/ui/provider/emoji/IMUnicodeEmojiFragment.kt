package com.thk.im.android.ui.provider.emoji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.thk.im.android.ui.databinding.FragmentPanelUnicodeEmojiBinding
import com.thk.im.android.ui.fragment.IMBaseEmojiFragment
import com.thk.im.android.ui.manager.IMUIManager

class IMUnicodeEmojiFragment : IMBaseEmojiFragment() {

    private lateinit var binding: FragmentPanelUnicodeEmojiBinding

    // 来自: https://emojixd.com/
    private var defaultEmojis = listOf<String>(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
        "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
        "😘", "😗", "☺️", "😚", "😙", "🥲", "😋", "😛",
        "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
        "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄",
        "😬", "🤥", "😶‍️", "😮‍💨", "😌", "😔", "😪", "🤤",
        "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "🥵",
        "🥶", "🥴", "😵", "🤯", "😵‍💫"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPanelUnicodeEmojiBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val gridLayoutManager = GridLayoutManager(context, 7)
        binding.rcvEmojis.layoutManager = gridLayoutManager

        val adapter = IMUnicodeEmojiAdapter()
        binding.rcvEmojis.adapter = adapter
        val emojis = IMUIManager.uiResourceProvider?.unicodeEmojis() ?: defaultEmojis
        adapter.setEmoji(emojis)
        adapter.onEmojiSelected = object : IMUnicodeEmojiAdapter.OnEmojiSelected {
            override fun onSelected(emoji: String) {
                getMsgSender()?.addInputContent(emoji)
            }
        }
    }
}