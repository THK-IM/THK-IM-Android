package com.thk.im.android.base.utils

import java.lang.StringBuilder

object StringUtils {

    fun byteArray2HexString(array: ByteArray): String {
        val hexStringBuilder = StringBuilder()
        for (b in array) {
            val st = String.format("%02X", b)
            hexStringBuilder.append(st)
        }
        return hexStringBuilder.toString()
    }

    fun getMessageCount(count: Int) : String? {
        return if (count <= 0) {
            return null
        } else if (count < 100) {
            "$count"
        } else {
            "99+"
        }
    }
}