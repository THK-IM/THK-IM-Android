package com.thk.im.android.core.signal;

import com.thk.im.android.core.SignalStatus;

public interface SignalModule {

    /**
     * 连接
     */
    void connect();

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    SignalStatus getSignalStatus();

    /**
     * 发送消息
     *
     * @param signal 消息内容
     * @throws RuntimeException 发送失败，抛出异常
     */
    void sendSignal(String signal) throws RuntimeException;

    /**
     * 设置信令接收器
     *
     * @param signalListener 信令接收器
     */
    void setSignalListener(SignalListener signalListener);

    /**
     * 断开连接
     *
     * @param reason 原因
     */
    void disconnect(String reason);

}
