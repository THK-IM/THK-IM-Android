package com.thk.im.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText

/**
 * 解决EditText android:imeOptions与inputType="textMultiLine" 的坑
 * https://blog.csdn.net/a641324093/article/details/62238385?locationNum=4&fps=1
 */
class MultiLineImeOptionEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val onCreateInputConnection = super.onCreateInputConnection(outAttrs)
        if (onCreateInputConnection != null) {
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
        }
        return onCreateInputConnection
    }
}