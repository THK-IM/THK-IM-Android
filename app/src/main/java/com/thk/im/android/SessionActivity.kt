package com.thk.im.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thk.im.android.db.entity.Session
import com.thk.im.android.ui.fragment.SessionFragment

class SessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        val tag = SessionFragment::class.java.name
        var fragment: SessionFragment? =
            supportFragmentManager.findFragmentByTag(tag) as SessionFragment?
        if (fragment == null) {
            fragment = SessionFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.fragment_container, fragment, tag)
            ft.commit()
        }
        fragment.setSessionClick(object : SessionFragment.OnSessionClick {
            override fun onSessionClick(session: Session) {
                val intent = Intent()
                intent.setClass(this@SessionActivity, MessageActivity::class.java)
                intent.putExtra("session", session)
                startActivity(intent)
            }
        })
    }
}