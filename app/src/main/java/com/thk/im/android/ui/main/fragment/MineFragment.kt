package com.thk.im.android.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thk.im.android.R
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.user.vo.UserVo
import com.thk.im.android.core.base.IMImageLoader
import com.thk.im.android.databinding.FragmentMineBinding
import com.thk.im.android.ui.base.BaseFragment

class MineFragment : BaseFragment() {

    private lateinit var binding: FragmentMineBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMineBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = DataRepository.getUser()
        user?.let {
            initUserInfo(it)
        }

        binding.navSetting.setIconTitle(R.drawable.ic_setting, "设置")
        binding.navAbout.setIconTitle(R.drawable.ic_about, "关于")

    }

    private fun initUserInfo(userVo: UserVo) {
        userVo.avatar?.let {
            IMImageLoader.displayImageUrl(binding.ivAvatar, it)
        }
        userVo.qrcode?.let {
            IMImageLoader.displayImageUrl(binding.ivQrcode, it)
        }
        binding.tvId.text = userVo.displayId
        if (userVo.nickname.isNullOrEmpty()) {
            binding.tvNickname.text = "无名"
        } else {
            binding.tvNickname.text = userVo.nickname
        }
    }
}