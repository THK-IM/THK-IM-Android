package com.thk.im.android.ui.emoji

interface IEmojiParser {
    /**
     * 获取所有的emoji资源ID
     */
    fun getEmojiIds(): IntArray

    /**
     * 获取所有表情文字
     */
    fun getEmojiTags(): Array<String>

    /**
     * 获取表情在资源组的下标
     */
    fun getIndexFromResId(emoId: Int): Int

    /**
     * 获取表情文字从资源下标
     */
    fun getTagFromResId(resId: Int): String

    /**
     * 通过表情文字获取表情资源
     */
    fun getEmoResIdFromTag(tag: String): Int


    /**
     * 替换表情文字为图片
     */
    fun emoCharSequence(text: CharSequence): CharSequence
}