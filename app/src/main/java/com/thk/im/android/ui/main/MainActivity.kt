package com.thk.im.android.ui.main

import android.os.Bundle
import com.thk.im.android.databinding.ActivityMainBinding
import com.thk.im.android.ui.base.BaseActivity

class MainActivity : BaseActivity() {


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}