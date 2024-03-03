package com.thk.im.android.core.exception

class ClientException(val codeMessage: CodeMessage) : RuntimeException()

val ParameterException = ClientException(CodeMessage(1, "param error"))
val DatabaseException = ClientException(CodeMessage(2, "database error"))
val FileReadException = ClientException(CodeMessage(3, "file read error"))
val FileWriteException = ClientException(CodeMessage(4, "file write error"))