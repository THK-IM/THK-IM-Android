package com.thk.im.android.core.signal;

public interface SignalListener {

    int StatusInit = 0;
    int StatusConnecting = 1;
    int StatusConnected = 2;
    int StatusDisConnected = 3;


    /**
     * 连接状态变更
     *
     * @param status 0:初始化, 1:连接中, 2:已连接, 3:连接已断开
     */
    void onStatusChange(int status);

    /**
     * 收到新消息
     *
     * @param type    消息类型
     * @param subType 消息子类型
     * @param msg     消息正文
     */
    void onNewMessage(int type, int subType, String msg);

}
