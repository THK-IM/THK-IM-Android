#include <jni.h>
#include "convertor.h"
#include "log.h"

JNIEXPORT jint JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeInit(JNIEnv *env, jobject instance,
                                           jint src_channels, jint src_sp_format_id,
                                           jint src_sp_rate,
                                           jint dst_channels, jint dst_sp_format_id,
                                           jint dst_sp_rate) {
    uint64_t src_ch;
    switch (src_channels) {
        case 1:
            src_ch = AV_CH_LAYOUT_MONO;
            break;

        case 2:
            src_ch = AV_CH_LAYOUT_STEREO;
            break;

        default:
            LOGE("Convertor : 初始化失败，不支持声道类型 %d", src_channels);
            return -1;
    }

    uint64_t dst_ch;
    switch (dst_channels) {
        case 1:
            dst_ch = AV_CH_LAYOUT_MONO;
            break;

        case 2:
            dst_ch = AV_CH_LAYOUT_STEREO;
            break;

        default:
            LOGE("Convertor : 初始化失败，不支持声道类型 %d", src_channels);
            return -2;
    }

    enum AVSampleFormat src_fmt = (enum AVSampleFormat) src_sp_format_id;
    enum AVSampleFormat dst_fmt = (enum AVSampleFormat) dst_sp_format_id;

    struct Convertor *convertor = convertor_init(src_ch, src_fmt, src_sp_rate,
                                                 dst_ch, dst_fmt, dst_sp_rate);

    jclass thclass = (*env)->GetObjectClass(env, instance);
    jfieldID fieldId = (*env)->GetFieldID(env, thclass, "nativeConvertorId", "J");
    if (convertor) {
        // 初始化成功
        (*env)->SetLongField(env, instance, fieldId, (jlong) convertor);
        return 0;
    } else {
        // 初始化失败
        (*env)->SetLongField(env, instance, fieldId, (jlong) 0);
        return -1;
    }
}

JNIEXPORT jint JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeFeedData(JNIEnv *env, jobject instance, jlong convertor_id,
                                                 jbyteArray data_, jint len) {
    if (convertor_id == 0) {
        LOGE("jni_convertor : Java_com_thk_im_android_live_player_PcmResample_nativeFeedData : convertor_id == 0");
        return -1;
    }
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    uint8_t *ds[1];
    ds[0] = (uint8_t *) data;

    struct Convertor *convertor = (struct Convertor *) convertor_id;
    int ret = convertor_feed_data(convertor, (uint8_t **) ds, len);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeGetConvertedSize(JNIEnv *env, jobject instance,
                                                           jlong convertor_id) {

    if (convertor_id == 0) {
        LOGE("jni_convertor : Java_com_thk_im_android_live_player_PcmResample_nativeGetConvertedSize : convertor_id == 0");
        return -1;
    }
    struct Convertor *convertor = (struct Convertor *) convertor_id;
    return convertor_get_converted_size(convertor);
}

JNIEXPORT jint JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeReceiveConvertedData(JNIEnv *env, jobject instance,
                                                               jlong convertor_id,
                                                               jbyteArray data_) {
    if (convertor_id == 0) {
        LOGE("jni_convertor : Java_com_whocttech_ffmpeg_Convertor__1receive_1converted_1data : convertor_id == 0");
        return -1;
    }
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    uint8_t *ds[1];
    ds[0] = (uint8_t *) data;

    struct Convertor *convertor = (struct Convertor *) convertor_id;
    int ret = convertor_receive_converted_data(convertor, (uint8_t **) ds);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return ret;
}

JNIEXPORT void JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeFlush(JNIEnv *env, jobject instance, jlong convertor_id) {
    if (convertor_id == 0) {
        LOGE("jni_convertor : Java_com_thk_im_android_live_player_PcmResample_nativeFlush : convertor_id == 0");
        return;
    }
    struct Convertor *convertor = (struct Convertor *) convertor_id;
    convertor_flush(convertor);
}

JNIEXPORT void JNICALL
Java_com_thk_im_android_live_player_PcmResample_nativeClose(JNIEnv *env, jobject instance, jlong convertor_id) {
    if (convertor_id == 0) {
        LOGE("jni_convertor : Java_com_thk_im_android_live_player_PcmResample_nativeClose : convertor_id == 0");
        return;
    }
// 将nativeDecoderId设置为0，防止重复调用close导致崩溃
    jclass thclass = (*env)->GetObjectClass(env, instance);
    jfieldID fieldId = (*env)->GetFieldID(env, thclass, "nativeConvertorId", "J");
    (*env)->SetLongField(env, instance, fieldId, (jlong) 0);

    struct Convertor *convertor = (struct Convertor *) convertor_id;
    convertor_close(convertor);
}
