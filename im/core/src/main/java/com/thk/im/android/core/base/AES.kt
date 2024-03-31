package com.thk.im.android.core.base

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES(private val key: String, private val iv: String) {

    private val padding = "AES/CBC/PKCS7Padding"
    private val ivParameterSpec = IvParameterSpec(iv.toByteArray(charset("utf-8")))  // 替换为实际的初始化向量
    private val charset = "utf-8"

    fun encrypt(strToEncrypt: String): String {
        val cipher = Cipher.getInstance(padding)
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(charset)), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(strToDecrypt: String): String {
        val cipher = Cipher.getInstance(padding)
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset(charset)), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT))
        return String(decryptedBytes, charset(charset))
    }
}