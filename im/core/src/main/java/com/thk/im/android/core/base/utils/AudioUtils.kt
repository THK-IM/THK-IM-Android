package com.thk.im.android.core.base.utils

import kotlin.math.abs
import kotlin.math.log10

object AudioUtils {

    private fun pcmToFloatArray(pcmData: ByteArray): FloatArray {
        val floatData = FloatArray(pcmData.size / 2)
        for ((j, i) in (pcmData.indices step 2).withIndex()) {
            val sample = (pcmData[i].toInt() and 0xFF) or (pcmData[i + 1].toInt() shl 8)
            floatData[j] = sample.toFloat()
        }
        return floatData
    }

    private fun calculateRMS(pcmData: ByteArray): Double {
        val floatData = pcmToFloatArray(pcmData)
        var totalAmplitude = 0.0
        for (sample in floatData) {
            totalAmplitude += abs(sample)
        }
        val averageAmplitude = totalAmplitude / floatData.size
        if (averageAmplitude <= 0) {
            return 0.0
        }
        return 20 * log10(averageAmplitude)
    }


    fun calculateDecibel(byteArray: ByteArray): Double {
        return calculateRMS(byteArray)
    }

}