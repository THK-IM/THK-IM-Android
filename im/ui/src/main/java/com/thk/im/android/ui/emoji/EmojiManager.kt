package com.thk.im.android.ui.emoji


import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.ui.R
import com.thk.im.android.ui.panel.emoji.bean.EmoBean
import com.thk.im.android.ui.panel.emoji.bean.EmoImageBean
import com.thk.im.android.ui.panel.emoji.bean.EmoTextBean
import java.util.*

/**
 * 表情管理类
 */
object EmojiManager {

    private const val RECENT_EMOJI_MAX_CAPACITY = 8

    private const val EMOJI_RECENT = "im_emoji"

    private const val USER_EMO_RECENT_LIST = "user_emo_recent_list"


    /**
     * 获取最近表情和全部表情
     */
    fun getEmojiList(): MutableList<EmoBean> {
        //获取所有表情数据
        val allEmojiList = getAllEmoList()
        //获取最近表情
        val recentEmojiList = getRecentEmojiList()
        allEmojiList.addAll(0, recentEmojiList)
        return allEmojiList
    }

    /**
     * 获取最近表情
     */
    fun getRecentEmojiList(): MutableList<EmoBean> {
        val recentEmojiList: MutableList<EmoBean> = mutableListOf()
        val recentEmojiIndexList = getRecentEmojiIndexes()
        if (recentEmojiIndexList.size > 0) {
            val emoImageBean = EmoTextBean()
            emoImageBean.title = IMCoreManager.getApplication().getString(R.string.latest_emoji)
            recentEmojiList.add(emoImageBean)
            val emoResIdList: IntArray = parser.getEmojiIds()
            for (indexValue in recentEmojiIndexList) {
                if (indexValue < emoResIdList.size) {
                    if (indexValue == -1) continue
                    val emoImage = EmoImageBean()
                    emoImage.emoId = emoResIdList.getOrNull(indexValue)
                    recentEmojiList.add(emoImage)
                }
            }
        }
        return recentEmojiList
    }

    /**
     * 获取全部表情，不包含最近表情
     */
    fun getAllEmoList(): MutableList<EmoBean> {
        // 获取表情数据
        val emoResIdList: IntArray = parser.getEmojiIds()
        val emojiList: MutableList<EmoBean> = mutableListOf()
        //全部表情标题
        val emoImageBean = EmoTextBean()
        emoImageBean.title = IMCoreManager.getApplication().getString(R.string.all_emoji)
        emojiList.add(emoImageBean)
        // 全部表情的id,添加到集合里面
        if (emoResIdList.isNotEmpty()) {
            for (emoId in emoResIdList) {
                val emoImage = EmoImageBean()
                emoImage.emoId = emoId
                emojiList.add(emoImage)
            }
        }
        return emojiList
    }

    /**
     * 获取最近表情的下标
     */
    fun getRecentEmojiIndexes(): LinkedList<Int> {
        val uid = IMCoreManager.getUid()
        val sharedPreferences =
            IMCoreManager.getApplication()
                .getSharedPreferences(EMOJI_RECENT + "$uid", Context.MODE_PRIVATE)
        val recentList = sharedPreferences.getString(USER_EMO_RECENT_LIST, "")
        return if (recentList.isNullOrEmpty()) {
            LinkedList()
        } else {
            Gson().fromJson(recentList, object : TypeToken<LinkedList<Int>>() {}.type)
        }
    }

    /**
     * 添加到最近的表情集合
     */
    fun addEmojiToRecent(emoId: Int) {
        val list: LinkedList<Int> = getRecentEmojiIndexes()
        //如果最近表情中已经包含该表情，将该表情移到第一个
        if (list.contains(emoId)) {
            list.remove(emoId)
        }
        list.addFirst(emoId)
        val lastList = if (list.size > RECENT_EMOJI_MAX_CAPACITY) {
            list.subList(0, RECENT_EMOJI_MAX_CAPACITY)
        } else {
            list
        }

        val toJSONString = Gson().toJson(lastList)
        val uid = IMCoreManager.getUid()
        val sharedPreferences =
            IMCoreManager.getApplication()
                .getSharedPreferences(EMOJI_RECENT + "$uid", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(USER_EMO_RECENT_LIST, toJSONString).apply()
    }

    lateinit var parser: IEmojiParser

    lateinit var application: Application

    fun init(instance: Application, parser: IEmojiParser) {
        this.application = instance
        this.parser = parser
    }
}