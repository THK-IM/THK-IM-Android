package com.thk.im.android.ui.panel.emoji

interface EmojiPanelCallback {
    /**
     * 表情点击回调
     */
    fun emojiOnItemClick(emoji: String)

    /**
     * 删除表情回调
     */
    fun deleteEmoji()
}