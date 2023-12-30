package com.thk.im.android.ui.group

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.thk.im.android.api.group.vo.GroupVo
import com.thk.im.android.databinding.ActivityGroupBinding
import com.thk.im.android.ui.base.BaseActivity

class GroupActivity : BaseActivity() {

    companion object {
        fun startGroupActivity(ctx: Context, groupVo: GroupVo) {
            val intent = Intent(ctx, GroupActivity::class.java)
            intent.putExtra("group", groupVo)
            ctx.startActivity(intent)
        }

        fun startCreateGroupActivity(ctx: Context) {
            val intent = Intent(ctx, GroupActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityGroupBinding
    private var groupVo: GroupVo? = null
    private var mode = 0 // 0创建/1查看/2编辑

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGroupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val initGroupVo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("group", GroupVo::class.java)
        } else {
            intent.getParcelableExtra("group")
        }
        if (initGroupVo == null) {
            mode = 0
            setTitle("创建群聊")
        }
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun needBackIcon(): Boolean {
        return true
    }


    override fun menuMoreVisibility(id: Int): Int {
        return View.GONE
    }

}