package com.thk.im.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thk.im.android.ui.fragment.IMSessionFragment

class SessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        val tag = IMSessionFragment::class.java.name
        var fragment: IMSessionFragment? =
            supportFragmentManager.findFragmentByTag(tag) as IMSessionFragment?
        if (fragment == null) {
            fragment = IMSessionFragment()
        }
        val bundle = Bundle()
        bundle.putLong("parentId", 0L)
        fragment.arguments = bundle
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.fragment_container, fragment, tag)
        ft.commit()
    }
}