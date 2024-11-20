package com.thk.im.android.core.base.utils

import java.nio.ByteBuffer

object BufferUtils {

    fun cloneByteBuffer(b: ByteBuffer): ByteBuffer {
        val clone = ByteBuffer.allocate(b.capacity())
        b.rewind()
        clone.put(b)
        b.rewind()
        clone.flip()
        return clone
    }
}