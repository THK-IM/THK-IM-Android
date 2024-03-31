package com.thk.im.android.module

import com.thk.im.android.core.Crypto
import com.thk.im.android.core.base.AES

class CipherCrypto(private val key: String, private val iv: String) : Crypto {

    private val aes = AES(key, iv)

    override fun encrypt(text: String): String? {
        val result = aes.encrypt(text)
        if (result.isEmpty()) {
            return null
        }
        return result
    }

    override fun decrypt(cipherText: String): String? {
        val result = aes.decrypt(cipherText)
        if (result.isEmpty()) {
            return null
        }
        return result
    }
}