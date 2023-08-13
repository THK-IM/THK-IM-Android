package com.thk.android.im.live.utils

import java.lang.StringBuilder

object Utils {

    fun byteArray2HexString(array: ByteArray): String {
        val hexStringBuilder = StringBuilder()
        for (b in array) {
            val st = String.format("%02X", b)
            hexStringBuilder.append(st)
        }
        return hexStringBuilder.toString()
    }
}