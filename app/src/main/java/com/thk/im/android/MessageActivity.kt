package com.thk.im.android

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thk.im.android.base.LLog
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.fragment.IMMessageFragment

class MessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LLog.v("onCreate MessageActivity $savedInstanceState")
        setContentView(R.layout.activity_message)
        val session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("session", Session::class.java)
        } else {
            intent.getParcelableExtra<Session>("session")
        }
        val tag = IMMessageFragment::class.java.name
        var fragment: IMMessageFragment? =
            supportFragmentManager.findFragmentByTag(tag) as IMMessageFragment?
        if (fragment == null) {
            fragment = IMMessageFragment().apply {
                arguments = Bundle().apply { putParcelable("session", session) }
            }
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.fragment_container, fragment, tag)
            ft.commit()
        }
    }

}