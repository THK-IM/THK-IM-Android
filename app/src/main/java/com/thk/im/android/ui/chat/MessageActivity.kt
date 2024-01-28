package com.thk.im.android.ui.chat

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.thk.android.im.live.Mode
import com.thk.im.android.R
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.entity.User
import com.thk.im.android.databinding.ActivityMessageBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.call.LiveCallActivity
import com.thk.im.android.ui.contact.ContactUserActivity
import com.thk.im.android.ui.fragment.IMMessageFragment
import com.thk.im.android.ui.group.GroupActivity

class MessageActivity : BaseActivity() {


    private lateinit var binding: ActivityMessageBinding

    private var user: User? = null
    private var group: Group? = null

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

        session?.let {
            initSessionTitle(it)
        }
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun needBackIcon(): Boolean {
        return true
    }

    override fun menuMoreVisibility(id: Int): Int {
        return View.VISIBLE
    }

    override fun menuIcon(id: Int): Drawable? {
        if (id == R.id.tb_menu1) {
            return ContextCompat.getDrawable(this, R.drawable.ic_call)
        } else if (id == R.id.tb_menu2) {
            return ContextCompat.getDrawable(this, R.drawable.ic_more)
        }
        return super.menuIcon(id)
    }

    override fun onToolBarMenuClick(view: View) {
        if (view.id == R.id.tb_menu2) {
            user?.let {
                ContactUserActivity.startContactUserActivity(this, it)
            }
            group?.let {
                GroupActivity.startGroupActivity(this, it)
            }
        } else if (view.id == R.id.tb_menu1) {
            user?.let {
                LiveCallActivity.startCallActivity(this, Mode.Video, it)
            }
        }
    }

    private fun initSessionTitle(session: Session) {
        if (session.type == SessionType.Single.value) {
            val subscribe = object : BaseSubscriber<User>() {
                override fun onNext(t: User?) {
                    t?.let {
                        setTitle(it.nickname)
                        user = it
                    }
                }
            }
            IMCoreManager.userModule.queryUser(session.entityId)
                .compose(RxTransform.flowableToMain()).subscribe(subscribe)
            addDispose(subscribe)
        } else {
            val subscribe = object : BaseSubscriber<Group>() {
                override fun onNext(t: Group?) {
                    t?.let {
                        setTitle(it.name)
                        group = it
                    }
                }
            }
            IMCoreManager.groupModule.findById(session.entityId)
                .compose(RxTransform.flowableToMain()).subscribe(subscribe)
            addDispose(subscribe)
        }
    }

}