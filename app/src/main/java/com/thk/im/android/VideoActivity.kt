package com.thk.im.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danikula.videocache.CacheListener
import com.thk.im.android.core.base.LLog
import com.thk.im.android.databinding.ActivityVideoBinding
import java.io.File

class VideoActivity : AppCompatActivity(), CacheListener {
    private lateinit var binding: ActivityVideoBinding
    private val videoUrl =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVideoView()
        com.thk.im.preview.VideoCache.getProxy().registerCacheListener(this, videoUrl)
    }

    private fun initVideoView() {
        binding.pvVideo.initPlay(videoUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pvVideo.releasePlay()
        com.thk.im.preview.VideoCache.getProxy().unregisterCacheListener(
            this, videoUrl
        )
    }

    override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
        LLog.v("onCacheAvailable $cacheFile, $percentsAvailable, $url")
    }
}