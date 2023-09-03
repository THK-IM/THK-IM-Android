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
    void onSignalStatusChange(int status);

    /**
     * 收到信令
     *
     * @param type    信令类型
     * @param subType 信令子类型
     * @param signal  信令正文
     */
    void onNewSignal(int type, int subType, String signal);

}
