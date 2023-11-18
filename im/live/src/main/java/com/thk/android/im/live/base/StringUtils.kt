package com.thk.android.im.live.base

import java.security.MessageDigest


object StringUtils {

    fun shaEncrypt(strSrc: String): String {
        val bt = strSrc.toByteArray()
        val md = MessageDigest.getInstance("SHA-1") // 将此换成SHA-1、SHA-512、SHA-384等参数
        md.update(bt)
        return byteArray2HexString(md.digest()) // to HexString
    }

    fun byteArray2HexString(array: ByteArray): String {
        val hexStringBuilder = StringBuilder()
        for (b in array) {
            val st = String.format("%02X", b)
            hexStringBuilder.append(st)
        }
        return hexStringBuilder.toString()
    }

    fun getMessageCount(count: Int): String? {
        return if (count <= 0) {
            return null
        } else if (count < 100) {
            "$count"
        } else {
            "99+"
        }
    }
}