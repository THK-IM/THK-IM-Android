package com.thk.im.android.opuslib;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import top.oply.opuslib.OpusEvent;
import top.oply.opuslib.OpusTool;
import top.oply.opuslib.OpusTrackInfo;
import top.oply.opuslib.Utils;

public class OpusRecorder {
    private static final String TAG = OpusRecorder.class.getName();
    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int RECORDER_SAMPLE_RATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static volatile OpusRecorder oRecorder;
    private final OpusTool opusTool = new OpusTool();
    private final ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);// Should be 1920, to accord with function writeFreme()
    private final Utils.AudioTime mRecordTime = new Utils.AudioTime();
    private volatile int state = STATE_NONE;
    private AudioRecord recorder = null;
    private Thread recordingThread = new Thread();
    private int bufferSize = 0;
    private String filePath = null;
    private OpusEvent mEventSender = null;
    private Timer mProgressTimer = null;
    private RecordCallback callBack = null;
    private int voiceValue = 0;
    private long maxVolumeStart = 0;
    private long maxVolumeEnd = 0;
    /**
     * 开始录制的时间戳
     */
    private long startTime = 0;
    /**
     * 停止录制的时间戳
     */
    private long endTime = 0;

    private OpusRecorder() {
    }

    public static OpusRecorder getInstance() {
        if (oRecorder == null)
            synchronized (OpusRecorder.class) {
                if (oRecorder == null)
                    oRecorder = new OpusRecorder();
            }
        return oRecorder;
    }

    public void setEventSender(OpusEvent es) {
        mEventSender = es;
    }

    public void setRecordCallback(RecordCallback cb) {
        callBack = cb;
    }


    /**
     * 开始录制
     *
     * @param file 音频文件保存的路径
     */
    @SuppressLint("MissingPermission")
    public void startRecording(final String file) {
        if (state == STATE_STARTED)
            return;
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            bufferSize = (minBufferSize / 1920 + 1) * 1920;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLE_RATE, RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, bufferSize);
            recorder.startRecording();
            state = STATE_STARTED;
            if (file.isEmpty()) {
                filePath = OpusTrackInfo.getInstance().getAValidFileName("OpusRecord");
            } else {
                filePath = file;
            }
            int rst = opusTool.startRecording(filePath);
            if (rst != 1) {
                if (mEventSender != null)
                    mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
                Log.e(TAG, "recorder initially error");
                return;
            }

            if (mEventSender != null) {
                mEventSender.sendEvent(OpusEvent.RECORD_STARTED);
            }
            //启动录制线程
            recordingThread = new Thread(new RecordThread(), "OpusRecord Thrd");
            recordingThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (mEventSender != null) {
                mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
                Log.e(TAG, "recorder initially error");
            }
        }
    }

    /**
     * 将音频数据写入到本地
     */
    private void writeAudioDataToFile() {
        final AudioRecord localRecorder = recorder;
        if (state != STATE_STARTED || localRecorder == null)
            return;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        short[] tempBuffer = new short[128];
        maxVolumeStart = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        while (state == STATE_STARTED) {
            endTime = System.currentTimeMillis();
            if (callBack != null) {
                callBack.updateProgress((endTime - startTime) / 1000f);
            }
            buffer.rewind();
            //读取流数据
            int len = localRecorder.read(buffer, bufferSize);
            int tmplen = 0;
            for (int i = 0; 2 * i + 1 < len; i++) {
                if (i >= 128) {
                    break;
                }
                short v = buffer.get(2 * i + 1);
                v = (short) (v << 8);
                v = (short) (v + buffer.get(2 * i));
                tempBuffer[i] = v;
                Log.i("tmp buf:", String.valueOf(tempBuffer[i]));

                tmplen = i + 1;
            }

            Log.d(TAG, "\n lengh of buffersize is " + len);
            if (len != AudioRecord.ERROR_INVALID_OPERATION) {
                try {
                    writeAudioDataToOpus(buffer, len);
                } catch (Exception e) {
                    if (mEventSender != null)
                        mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
                    Utils.printE(TAG, e);
                }
            }
            maxVolumeEnd = System.currentTimeMillis();

            setMaxVolume(tempBuffer, tmplen);
        }
    }

    /**
     * 将音频数据转成opus格式写入
     *
     * @param buffer buffer
     * @param size   size
     */
    private void writeAudioDataToOpus(ByteBuffer buffer, int size) {
        ByteBuffer finalBuffer = ByteBuffer.allocateDirect(size);
        finalBuffer.put(buffer);
        finalBuffer.rewind();
        boolean flush = false;

        //write data to Opus file
        while (state == STATE_STARTED && finalBuffer.hasRemaining()) {
            int oldLimit = -1;
            if (finalBuffer.remaining() > fileBuffer.remaining()) {
                oldLimit = finalBuffer.limit();
                finalBuffer.limit(fileBuffer.remaining() + finalBuffer.position());
            }
            fileBuffer.put(finalBuffer);
            if (fileBuffer.position() == fileBuffer.limit() || flush) {
                int length = !flush ? fileBuffer.limit() : finalBuffer.position();

                int rst = opusTool.writeFrame(fileBuffer, length);
                if (rst != 0) {
                    fileBuffer.rewind();
                }
            }
            if (oldLimit != -1) {
                finalBuffer.limit(oldLimit);
            }
        }
    }

    private void setMaxVolume(short[] buffer, int readLen) {
        try {
            if (maxVolumeEnd - maxVolumeStart < 100) {
                return;
            }
            maxVolumeStart = maxVolumeEnd;
            int max = 0;
            for (int i = 0; i < readLen; i++) {
                if (Math.abs(buffer[i]) > max) {
                    max = Math.abs(buffer[i]);
                }
            }
            voiceValue = max;
            if (callBack != null) {
                callBack.updateVolume(voiceValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 停止录制
     */
    public void stopRecording() {
        if (state != STATE_STARTED) {
            return;
        }

        state = STATE_NONE;
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
        }
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }

        if (null != recorder) {
            opusTool.stopRecording();
            recordingThread = null;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        updateTrackInfo();
        if (callBack != null) {
            callBack.onRecordFinish((endTime - startTime) / 1000.0f);
        }
    }

    private void updateTrackInfo() {
        OpusTrackInfo info = OpusTrackInfo.getInstance();
        info.addOpusFile(filePath);
        if (mEventSender != null) {
            File f = new File(filePath);
            mEventSender.sendEvent(OpusEvent.RECORD_FINISHED, f.getName());
        }
    }

    public boolean isWorking() {
        return state != STATE_NONE;
    }

    public void release() {
        if (state != STATE_NONE) {
            stopRecording();
        }
    }

    class RecordThread implements Runnable {
        public void run() {
            //记录时间
            mProgressTimer = new Timer();
            mRecordTime.setTimeInSecond(0);
            mProgressTimer.schedule(new MyTimerTask(), 1000, 1000);

            try {
                writeAudioDataToFile();
            } catch (Exception e) {
                e.printStackTrace();
                if (mEventSender != null) {
                    mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
                }
            }
        }
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            if (state != STATE_STARTED) {
                if (mProgressTimer != null) {
                    mProgressTimer.cancel();
                }
            } else {
                mRecordTime.add(1);
                String progress = mRecordTime.getTime();
                if (mEventSender != null)
                    mEventSender.sendRecordProgressEvent(progress);
            }
        }
    }

    public interface RecordCallback {
        void updateProgress(float duration);

        void updateVolume(int voiceValue);

        void onRecordFinish(float duration);
    }

}
