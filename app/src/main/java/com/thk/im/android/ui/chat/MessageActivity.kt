package com.thk.im.android.ui.chat

import android.os.Build
import android.os.Bundle
import com.thk.im.android.R
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.databinding.ActivityMessageBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.fragment.IMMessageFragment

class MessageActivity : BaseActivity() {


    private lateinit var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val session = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("session", Session::class.java)
        } else {
            intent.getParcelableExtra("session")
        }
        val tag = IMMessageFragment::class.java.name
        var fragment: IMMessageFragment? =
            supportFragmentManager.findFragmentByTag(tag) as IMMessageFragment?
        if (fragment == null) {
            fragment = IMMessageFragment()
        }
        fragment.arguments = Bundle().apply { putParcelable("session", session) }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(binding.fragmentContainer.id, fragment, tag)
        ft.commit()
    }

}