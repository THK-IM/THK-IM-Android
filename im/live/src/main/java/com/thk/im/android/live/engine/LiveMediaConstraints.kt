package com.thk.im.android.live.engine

import org.webrtc.MediaConstraints

object LiveMediaConstraints {

    fun build(
        enable3a: Boolean,
        enableCpu: Boolean,
        enableGainControl: Boolean,
        enableStereo: Boolean,
    ): MediaConstraints {
        val constraints = MediaConstraints()
        val enable3aStr = if (enable3a) "true" else "false"
        //回声消除
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation2",
                enable3aStr
            )
        )
        //高音过滤
        constraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", enable3aStr))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("googAudioMirroring", enable3aStr))
        //噪音处理
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googTypingNoiseDetection",
                enable3aStr
            )
        )
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression2",
                enable3aStr
            )
        )

        val enableCpuStr = if (enableCpu) "true" else "false"
        //cpu过载监控
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googCpuOveruseDetection",
                enableCpuStr
            )
        )

        val enableGainControlStr = if (enableGainControl) "true" else "false"
        //自动增益
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googAutoGainControl2",
                enableGainControlStr
            )
        )

        val enableStereoStr = if (enableStereo) "true" else "false"
        constraints.mandatory.add(MediaConstraints.KeyValuePair("stereo", enableStereoStr)) // 启用立体声
        return constraints
    }

    fun offerOrAnswerConstraint(isReceive: Boolean, enableStereo: Boolean): MediaConstraints {
        val mediaConstraints = MediaConstraints()
        //是否接收音频
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", isReceive.toString()
            )
        )
        //是否接收视频
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", isReceive.toString()
            )
        )
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("googCpuOveruseDetection", "true")
        )
        val enableStereoStr = if (enableStereo) "true" else "false"
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "stereo",
                enableStereoStr
            )
        ) // 启用立体声
        return mediaConstraints
    }

}