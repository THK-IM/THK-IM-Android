package com.thk.im.android.core.signal;

public interface SignalModule {

    /**
     * 设置服务器时间
     * @param time
     */
    void setSeverTime(long time);

    /**
     * 获取当前服务器时间
     * @return long
     */
    long getSeverTime();

    /**
     * 获取连接id
     * @return nullable
     */
    String getConnId();

    /**
     * 设置连接时间
     * @param connId
     */
    void setConnId(String connId);

    /**
     * 更新token
     *
     * @param token 连接令牌
     */
    void updateToken(String token);

    /**
     * 连接
     */
    void connect();

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    int getConnectStatus();

    /**
     * 发送消息
     *
     * @param msg 消息内容
     * @throws RuntimeException 发送失败，抛出异常
     */
    void sendMessage(String msg) throws RuntimeException;

    /**
     * 设置信令接收器
     *
     * @param signal 信令接收器
     */
    void setSignalListener(SignalListener signal);

    /**
     * 断开连接
     *
     * @param reason 原因
     */
    void disconnect(String reason);

}
