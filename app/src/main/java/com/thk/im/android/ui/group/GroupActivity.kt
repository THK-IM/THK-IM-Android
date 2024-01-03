package com.thk.im.android.ui.group

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.thk.im.android.R
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.group.vo.CreateGroupVo
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SessionType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.extension.setShape
import com.thk.im.android.core.db.entity.Group
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.databinding.ActivityGroupBinding
import com.thk.im.android.ui.base.BaseActivity
import com.thk.im.android.ui.contact.ContactActivity
import com.thk.im.android.ui.group.adapter.GroupMemberAdapter
import com.thk.im.android.ui.main.fragment.ContactFragment
import io.reactivex.Flowable


class GroupActivity : BaseActivity() {

    companion object {
        fun startGroupActivity(ctx: Context, group: Group) {
            val intent = Intent(ctx, GroupActivity::class.java)
            intent.putExtra("group", group)
            ctx.startActivity(intent)
        }

        fun startCreateGroupActivity(ctx: Context) {
            val intent = Intent(ctx, GroupActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityGroupBinding
    private var mode = 0 // 0创建/1查看或编辑

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGroupBinding.inflate(layoutInflater)
        val group = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("group", Group::class.java)
        } else {
            intent.getParcelableExtra("group")
        }
        mode = if (group == null) {
            0
        } else {
            1
        }
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (group == null) {
            initCreateGroupUI()
        } else {
            initShowGroupUI(group)
        }
    }

    override fun getToolbar(): Toolbar {
        return binding.tbTop.toolbar
    }

    override fun needBackIcon(): Boolean {
        return true
    }


    override fun menuMoreVisibility(id: Int): Int {
        if (id == R.id.tb_menu1) {
            return if (mode == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        return View.GONE
    }

    override fun menuIcon(id: Int): Drawable? {
        return if (id == R.id.tb_menu2) {
            ContextCompat.getDrawable(this, R.drawable.ic_add)
        } else  {
            ContextCompat.getDrawable(this, R.drawable.ic_choose)
        }
    }

    override fun onToolBarMenuClick(view: View) {
        if (view.id == R.id.tb_menu1) {
            if (mode == 0) {
                // 创建群
                createGroup()
            }
        }
    }

    private fun createGroup() {
        val groupName = binding.etNameInput.text.toString()
        val announce = binding.etAnnounceInput.text.toString()
        val ids = getMemberIds()
        val uId = IMCoreManager.uId

        val createGroupVo = CreateGroupVo(uId, ids, groupName, announce, SessionType.Group.value)
        val subscriber = object : BaseSubscriber<Group>() {
            override fun onNext(t: Group?) {
            }

            override fun onComplete() {
                super.onComplete()
                dismissLoading()
                removeDispose(this)
            }
        }

        DataRepository.groupApi.createGroup(createGroupVo)
            .flatMap {
                val group = it.toGroup()
                IMCoreManager.db.groupDao().insertOrUpdateGroups(listOf(group))
                return@flatMap Flowable.just(group)
            }
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
        showLoading()
    }

    private fun initCreateGroupUI() {
        setTitle("创建群聊")
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val ids = result.data?.getLongArrayExtra("ids")
                ids?.let {
                    showMembers(it)
                }
            }
        }

        binding.lyGroupAvatar.visibility = View.GONE
        binding.etAnnounceInput.setShape(
            Color.parseColor("#333333"),
            Color.parseColor("#ffffff"),
            1,
            floatArrayOf(5f, 5f, 5f, 5f)
        )

        binding.lyGroupMember.setOnClickListener {
            ContactActivity.openContactActivity(this@GroupActivity, 1, launcher)
        }

        val adapter = GroupMemberAdapter(this)
        val layoutManager = GridLayoutManager(this, 5)
        binding.rcvMembers.adapter = adapter
        binding.rcvMembers.layoutManager = layoutManager
    }

    private fun initShowGroupUI(group: Group) {
        setTitle(group.name)
        val groupName = group.name
        binding.etNameInput.setText(groupName)

        binding.lyGroupAvatar.visibility = View.VISIBLE
        IMImageLoader.displayImageUrl(binding.ivAvatarInput, group.avatar)

        binding.etAnnounceInput.setShape(
            Color.parseColor("#333333"),
            Color.parseColor("#ffffff"),
            1,
            floatArrayOf(5f, 5f, 5f, 5f)
        )
        binding.etAnnounceInput.setText(group.announce)

        binding.lyGroupMember.setOnClickListener {
        }

        val adapter = GroupMemberAdapter(this)
        val layoutManager = GridLayoutManager(this, 5)
        binding.rcvMembers.adapter = adapter
        binding.rcvMembers.layoutManager = layoutManager

        val subscriber = object :BaseSubscriber<List<SessionMember>>() {
            override fun onNext(t: List<SessionMember>?) {
                t?.let {
                    val ids = mutableListOf<Long>()
                    for (member in it) {
                        ids.add(member.userId)
                    }
                    showMembers(ids.toLongArray())
                }
            }

            override fun onComplete() {
                super.onComplete()
                removeDispose(this)
                IMCoreManager.messageModule.syncSessionMembers(group.sessionId)
            }
        }
        IMCoreManager.messageModule.querySessionMembers(group.sessionId)
            .compose(RxTransform.flowableToMain())
            .subscribe(subscriber)
        addDispose(subscriber)
    }

    private fun showMembers(ids: LongArray) {
        val adapter = binding.rcvMembers.adapter as GroupMemberAdapter
        adapter.addIds(ids.asList())
    }

    private fun getMemberIds(): List<Long> {
        val adapter = binding.rcvMembers.adapter as GroupMemberAdapter
        return adapter.ids
    }

}