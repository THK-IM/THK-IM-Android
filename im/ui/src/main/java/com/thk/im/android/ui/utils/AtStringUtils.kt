package com.thk.im.android.ui.utils


typealias NicknameFinder = (Long) -> String

typealias IdFinder = (String) -> Long

object AtStringUtils {

    val atRegex = "(?<=@)(.+?)(?=\\s)".toRegex()

    fun replaceAtNickNamesToUIds(text: String, finder: IdFinder): Pair<String, String?> {
        var atUIdsStr: String? = null
        val body = atRegex.replace(text) { result ->
            val id = finder(result.value)
            atUIdsStr = if (atUIdsStr == null) {
                "$id"
            } else {
                "$atUIdsStr#$id"
            }
            return@replace "$id"
        }
        return Pair(body, atUIdsStr)
    }

    fun replaceAtUIdsToNickname(text: String, atUIds: String, finder: NicknameFinder): String {
        val body = atRegex.replace(text) { result ->
            if (!atUIds.contains(result.value)) return@replace ""
            val id = result.value.toLongOrNull() ?: return@replace ""
            return@replace finder(id)
        }
        return body
    }
}