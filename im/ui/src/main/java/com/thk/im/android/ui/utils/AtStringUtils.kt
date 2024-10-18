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

    fun replaceAtUIdsToNickname(text: String, atUIds: Set<Long>, finder: NicknameFinder): String {
        val body = atRegex.replace(text) { result ->
            val id = result.value.toLongOrNull()
            if (id != null) {
                if (!atUIds.contains(id)) return@replace result.value
                return@replace finder(id)
            }
            result.value
        }
        return body
    }
}