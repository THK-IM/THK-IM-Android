package com.thk.android.im.live.utils

import org.webrtc.MediaConstraints

object MediaConstraintsHelper {

    fun build(
        enable3a: Boolean,
        enableCpu: Boolean,
        enableGainControl: Boolean
    ): MediaConstraints {
        val constraints = MediaConstraints()
        val enable3aStr = if (enable3a) "true" else "false"
        //回声消除
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                enable3aStr
            )
        )
        //高音过滤
        constraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", enable3aStr))
        //噪音处理
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
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
                "googAutoGainControl",
                enableGainControlStr
            )
        )
        return constraints
    }

    fun offerOrAnswerConstraint(isReceive: Boolean): MediaConstraints {
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
        return mediaConstraints
    }

}