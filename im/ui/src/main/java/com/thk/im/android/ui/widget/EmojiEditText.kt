package com.thk.im.android.ui.widget


import android.content.ClipboardManager
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import com.thk.im.android.ui.emoji.EmojiManager

/**
 * 复制表情会直接在输入框转成表情图标
 */
class EmojiEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    val atManager = AtManager()


    /**
     * 是否可以@人
     */
    var enableAt: Boolean = true

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == ID_PASTE) {
            //粘贴时将文字转成表情
            try {
                val value = clipboardMsg
                val edit = editableText
                edit.append(EmojiManager.parser.emoCharSequence(value))
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onTextContextMenuItem(id)
    }

    private var mAtListener: AtListener? = null

    private var beforeText: String = ""
    private var afterText: String = ""

    /**
     * 注册艾特的回调
     */
    fun registerAtListener(atListener: AtListener) {
        if (!enableAt) {
            Log.w(TAG, "plz set enableAt true to support at feature")
            return
        }
        this.mAtListener = atListener
        watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforeText = s.toString()
                Log.d(
                    TAG,
                    "beforeTextChanged===>" + s?.toString()
                )
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(
                    TAG,
                    "onTextChanged===>" + text.toString() + ",start:" + start + ",lengthBefore:" + start + ",lengthAfter:" + start
                )
            }

            override fun afterTextChanged(s: Editable?) {
                afterText = s.toString()
                Log.d(
                    TAG,
                    "afterTextChanged===>" + s?.toString()
                )
                if ((afterText.length - beforeText.length) == 1 && afterText.last()
                        .toString() == "@"
                ) {
                    atListener.showAtPanel()
                }
            }
        }
        addTextChangedListener(watcher)
    }

    /**
     * 取消注销艾特的回调
     */
    fun unregisterAtListener() {
        this.mAtListener = null
        watcher?.let {
            removeTextChangedListener(it)
        }
    }

    interface AtListener {
        fun showAtPanel()
    }

    private val clipboardMsg: String
        get() {
            var clipboardText = ""
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return clipboardText
            val data = clipboard.primaryClip
            if (null == data || data.itemCount <= 0) {
                return clipboardText
            }
            val item = data.getItemAt(0) ?: return clipboardText
            val text = item.text ?: return clipboardText
            clipboardText = text.toString()
            return clipboardText
        }

    companion object {
        private const val ID_PASTE = android.R.id.paste
        const val TAG = "EmojiEditText"
    }

    private var watcher: TextWatcher? = null


    inner class AtManager {

        var data: List<AtUserBean> = emptyList()

        fun setAtUserData(data: List<AtUserBean>) {
            this.data = data
        }

        fun addAtUserData(data: AtUserBean) {
            this.data = this.data.toMutableList().apply {
                add(data)
            }
        }
    }

    /**
     * 保存艾特用户的信息
     */
    interface AtUserBean
}