package com.thk.im.android.ui.contact

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.thk.im.android.R
import com.thk.im.android.databinding.ActivityContactBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.main.fragment.ContactFragment

class ContactListActivity : BaseActivity() {

    companion object {
        fun openContactListActivity(
            ctx: Context,
            mode: Int,
            launcher: ActivityResultLauncher<Intent>?
        ) {
            val intent = Intent(ctx, ContactListActivity::class.java)
            intent.putExtra("mode", mode)
            if (launcher != null) {
                launcher.launch(intent)
            } else {
                ctx.startActivity(intent)
            }
        }
    }

    private lateinit var binding: ActivityContactBinding
    private var mode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityContactBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mode = intent.getIntExtra("mode", 0)
        if (mode == 1) {
            setTitle("选择联系人")
        }


        val tag = ContactFragment::class.java.name
        var fragment: ContactFragment? =
            supportFragmentManager.findFragmentByTag(tag) as ContactFragment?
        if (fragment == null) {
            fragment = ContactFragment()
        }
        fragment.arguments = Bundle().apply { putInt("mode", mode) }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(binding.fragmentContainer.id, fragment, tag)
        ft.commit()
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun needBackIcon(): Boolean {
        return true
    }

    override fun menuMoreVisibility(id: Int): Int {
        if (id == R.id.tb_menu2) {
            return View.GONE
        } else if (id == R.id.tb_menu1) {
            return View.VISIBLE
        }
        return View.VISIBLE
    }

    override fun menuIcon(id: Int): Drawable? {
        return if (id == R.id.tb_menu2) {
            ContextCompat.getDrawable(this, R.drawable.ic_add)
        } else {
            ContextCompat.getDrawable(this, R.drawable.ic_choose)
        }
    }

    override fun onToolBarMenuClick(view: View) {
        if (view.id == R.id.tb_menu2) {
        } else if (view.id == R.id.tb_menu1) {
            if (mode == 1) {
                val tag = ContactFragment::class.java.name
                val fragment: ContactFragment? =
                    supportFragmentManager.findFragmentByTag(tag) as ContactFragment?
                fragment?.let {
                    val ids = fragment.getSelectedIds()
                    val data = Intent()
                    data.putExtra("ids", ids)
                    setResult(RESULT_OK, data)
                    finish()
                }

            }
        }
    }

}