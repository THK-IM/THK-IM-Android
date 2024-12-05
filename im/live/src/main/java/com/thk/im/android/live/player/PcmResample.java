package com.thk.im.android.live.player;

import androidx.annotation.Keep;

@Keep
public class PcmResample {
    static {
        System.loadLibrary("live");
    }

    @Keep
    private final long nativeConvertorId = 0L;

    @Keep
    private native int nativeInit(int src_channels, int src_sp_format_id, int src_sp_rate,
                                  int dst_channels, int dst_sp_format_id, int dst_sp_rate);

    @Keep
    private native int nativeFeedData(long convertor_id, byte[] data, int len);

    @Keep
    private native int nativeGetConvertedSize(long convertor_id);

    @Keep
    private native int nativeReceiveConvertedData(long convertor_id, byte[] data);

    @Keep
    private native void nativeFlush(long convertor_id);

    @Keep
    private native void nativeClose(long convertor_id);

    public int srcChannel;
    public int srcSpRate;
    public int dstChannel;
    public int dstSpRate;

    public int init(int srcChannel, int srcSpRate, int dstChannel, int dstSpRate) {
        return init(srcChannel, 1, srcSpRate, dstChannel, 1, dstSpRate);
    }

    public int init(int srcChannel, int srcSpFormat, int srcSpRate,
                    int dstChannel, int dstSpFormat, int dstSpRate) {
        this.srcChannel = srcChannel;
        this.srcSpRate = srcSpRate;
        this.dstChannel = dstChannel;
        this.dstSpRate = dstSpRate;
        return nativeInit(srcChannel, srcSpFormat, srcSpRate,
                dstChannel, dstSpFormat, dstSpRate);
    }


    public boolean support(int srcChannel, int srcSpRate, int dstChannel, int dstSpRate) {
        return this.srcChannel == srcChannel
                && this.srcSpRate == srcSpRate
                && this.dstChannel == dstChannel
                && this.dstSpRate == dstSpRate;
    }

    public int feedData(byte[] data, int len) {
        return nativeFeedData(nativeConvertorId, data, len);
    }

    public int getConvertedSize() {
        return nativeGetConvertedSize(nativeConvertorId);
    }

    public int receiveConvertedData(byte[] data) {
        return nativeReceiveConvertedData(nativeConvertorId, data);
    }

    public void flush() {
        nativeFlush(nativeConvertorId);
    }

    public void close() {
        nativeClose(nativeConvertorId);
    }
}
