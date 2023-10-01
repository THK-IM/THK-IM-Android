package com.thk.im.android.ui.provider.internal.panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.thk.im.android.ui.databinding.FragmentPanelUnicodeEmojiBinding
import com.thk.im.android.ui.fragment.panel.BasePanelFragment
import com.thk.im.android.ui.protocol.IMMsgSender

class IMUnicodeEmojiFragment(msgSender: IMMsgSender) : BasePanelFragment(msgSender) {

    private lateinit var binding: FragmentPanelUnicodeEmojiBinding

    // 来自: https://emojixd.com/
    private var emojis = listOf<String>(
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
        val gridLayoutManager = GridLayoutManager(context, 8)
        binding.rcvEmojis.layoutManager = gridLayoutManager

        val adapter = IMUnicodeEmojiAdapter()
        binding.rcvEmojis.adapter = adapter
        adapter.setEmoji(emojis)
    }
}