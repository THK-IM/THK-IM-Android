package com.thk.im.android.core.storage.internal

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.storage.StorageModule
import okhttp3.internal.and
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest


class DefaultStorageModule(private val app: Application, private val uid: Long) : StorageModule {

    private val rootPath: String = "${app.applicationContext.filesDir.path}/im/$uid"
    private val rootDir: File = File(rootPath)

    init {
        for (type in 1..3) {
            val dir = File("${rootPath}/avatar/${type}")
            if (dir.exists() && !dir.isDirectory) {
                dir.delete()
            } else {
                dir.mkdirs()
            }
        }
    }

    private fun toHexString(ba: ByteArray): String {
        val sb = StringBuilder()
        for (b in ba) {
            var hexChar = Integer.toHexString(b and 0xff)
            if (hexChar.length == 1) {
                hexChar = "0$hexChar"
            }
            sb.append(hexChar)
        }

        return sb.toString();
    }

    private fun getFileName(url: String): String {
        val b64Url = Base64.encode(url.toByteArray(Charset.forName("UTF-8")), Base64.DEFAULT)
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(b64Url)
        return toHexString(digest)
    }

    private fun getFileExt(fileName: String): Pair<String, String> {
        val i = fileName.lastIndexOf(".")
        return if (i <= 0 || i >= fileName.length) {
            Pair(fileName, "")
        } else {
            Pair(fileName.substring(0, i), fileName.substring(i + 1))
        }
    }

    override fun getPathsFromFullPath(fullPath: String): Pair<String, String>? {
        val i = fullPath.lastIndexOf("/")
        return if (i <= 0 || i >= fullPath.length) {
            null
        } else {
            Pair(fullPath.substring(0, i), fullPath.substring(i + 1))
        }
    }

    private fun getSessionRootPath(sid: Long): String {
        return "$rootPath/session-${sid}"
    }

    override fun getFileExtFromUrl(url: String): String {
        val uRL = URL(url)
        val path = uRL.path
        return getFileExt(path).second
    }

    @Throws(IOException::class)
    override fun saveImageInto(fullPath: String, b: Bitmap) {
        val pair = getPathsFromFullPath(fullPath) ?: throw IOException("path error")
        val dirFile = File(pair.first)
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw IOException("create dir $fullPath failed")
            }
        }
        val file = File(fullPath)
        if (file.exists()) {
            if (!file.delete()) {
                throw IOException("delete file ${file.absolutePath} failed")
            }
        }
        if (!file.createNewFile()) {
            throw IOException("create file ${file.absolutePath} failed")
        }
        val outputFileStream = FileOutputStream(fullPath)
        b.compress(Bitmap.CompressFormat.JPEG, 100, outputFileStream)
        outputFileStream.flush()
        outputFileStream.close()
    }

    @Throws(IOException::class)
    override fun saveInto(fullPath: String, b: ByteArray) {
        val pair = getPathsFromFullPath(fullPath)
        if (pair != null) {
            saveInto(pair.first, pair.second, b)
        } else {
            throw IOException("path error")
        }
    }

    @Throws(IOException::class)
    override fun saveInto(dir: String, name: String, b: ByteArray) {
        val dirFile = File(dir)
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw IOException("create dir $dir failed")
            }
        }

        val file = File("$dir/$name")
        if (file.exists()) {
            if (file.createNewFile()) {
                throw IOException("create file ${file.absolutePath} failed")
            }
        }

        val outputStream = FileOutputStream(file)
        outputStream.write(b)
        outputStream.flush()
        outputStream.close()
    }

    override fun copyFile(srcPath: String, desPath: String): Boolean {
        var input: InputStream? = null
        var output: OutputStream? = null
        var res = false
        try {
            val desFile = File(desPath)
            if (!desFile.exists()) {
                if (!desFile.createNewFile()) {
                    return false
                }
            }
            input = FileInputStream(File(srcPath))
            output = FileOutputStream(File(desPath))
            val buf = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buf).also { bytesRead = it } > 0) {
                output.write(buf, 0, bytesRead)
            }
            res = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
            output?.close()
        }
        return res
    }

    override fun allocAvatarPath(id: Long, avatarUrl: String, type: Int): String {
        var ext = getFileExtFromUrl(avatarUrl)
        if (ext == "") {
            ext = "jpeg"
        }
        return "${rootPath}/avatar/${type}/user-${id}.${ext}"
    }

    @Throws(IOException::class)
    override fun allocSessionFilePath(
        sid: Long,
        uid: Long,
        fileName: String,
        format: String
    ): Pair<String, String> {
        val key =
            "im/session_${sid}/${uid}/" + IMCoreManager.getSignalModule().severTime + "_${fileName}"

        val rootPath = "${getSessionRootPath(sid)}/$format"
        val dir = File(rootPath)
        if (dir.exists()) {
            if (dir.isDirectory) {
                val p = getFileExt(fileName)
                val name = p.first
                val ext = ".${p.second}"
                val fullPath = "/$rootPath/${name}"
                var i: Int? = null
                while (true) {
                    if (i == null) {
                        val file = File("${fullPath}$ext")
                        if (file.exists()) {
                            i = 1
                        } else {

                            return Pair(file.absolutePath, key)
                        }
                    } else {
                        val file = File("${fullPath}.$i$ext")
                        if (file.exists()) {
                            i++
                        } else {
                            return Pair(file.absolutePath, key)
                        }
                    }
                }
            }
        } else {
            dir.mkdirs()
        }
        return Pair("$rootPath/$fileName", key)
    }

    override fun allocServerFilePath(
        sid: Long,
        uid: Long,
        fileName: String
    ): String {
        return "im/session_${sid}/${uid}/" + IMCoreManager.getSignalModule().severTime + "_${fileName}"
    }

    override fun allocLocalFilePath(sid: Long, fileName: String, format: String): String {
        val rootPath = "${getSessionRootPath(sid)}/$format"
        val dir = File(rootPath)
        if (dir.exists()) {
            if (dir.isDirectory) {
                val p = getFileExt(fileName)
                val name = p.first
                val ext = ".${p.second}"
                val fullPath = "/$rootPath/${name}"
                var i: Int? = null
                while (true) {
                    if (i == null) {
                        val file = File("${fullPath}$ext")
                        if (file.exists()) {
                            i = 1
                        } else {
                            return file.absolutePath
                        }
                    } else {
                        val file = File("${fullPath}.$i$ext")
                        if (file.exists()) {
                            i++
                        } else {
                            return file.absolutePath
                        }
                    }
                }
            }
        } else {
            dir.mkdirs()
        }
        return "$rootPath/$fileName"
    }

    override fun isAssignedPath(
        path: String,
        fileName: String,
        format: String, sid: Long
    ): Boolean {
        val p = "${getSessionRootPath(sid)}/$format"
        return path.startsWith(p)
    }

    override fun getSessionCacheFiles(format: String, sid: Long): List<File> {
        return emptyList()
    }

    override fun getSessionCacheSize(sid: Long): Long {
        return 0L
    }
}