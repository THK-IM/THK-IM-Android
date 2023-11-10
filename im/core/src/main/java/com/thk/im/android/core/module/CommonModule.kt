package com.thk.im.android.core.module

interface CommonModule: BaseModule {

    /**
     * 设置服务器时间
     * @param time
     */
    fun setSeverTime(time: Long)

    /**
     * 获取当前服务器时间
     * @return long
     */
    fun getSeverTime(): Long

}