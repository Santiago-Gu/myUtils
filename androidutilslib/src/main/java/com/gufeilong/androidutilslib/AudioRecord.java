package com.gufeilong.androidutilslib;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Gu Feilong on 17-1-3.
 */
public class AudioRecord {

    /**
     * 缓存文件
     ***/
    private File audioFile;
    /**
     * 缓存文件路径
     ***/
    private String filePath;
    private boolean isRecording = true, isPlaying = false; //标记

    private int frequence = 48 * 1000; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 是否是录音
     ***/
    public static boolean isRecord = false;
    public static boolean isSaveFile = false;
    private RecordTask recorder;
    private PlayTask player;
    private long recordTime = 0;
    private OnRecordProgressLisenter progressLisenter;

    private static AudioRecord audioRecord;

    public static AudioRecord getInstance() {
        if (null == audioRecord) {
            audioRecord = new AudioRecord();
        }
        return audioRecord;
    }

    /**
     * 开始录音和播放
     **/
    public void startRecordAndPlay() {
        //这里启动录制任务
        recorder = new RecordTask();
        //recorder.execute();
        recorder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

/*        player = new PlayTask();
        player.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
    }

    /**
     * 停止录音和播放
     ***/
    public void stopRecordAndPlay() {
        this.isRecording = false;
        this.isPlaying = false;
    }

    public void setProgressLisenter(OnRecordProgressLisenter progressLisenter) {
        this.progressLisenter = progressLisenter;
    }

    private ArrayList<Short> list = new ArrayList<Short>();

    class RecordTask extends AsyncTask<Void, Long, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            Log.e("gufl", " start record ");
            isRecording = true;
            recordTime = System.currentTimeMillis();
            DataOutputStream dos = null;
            try {
                //如果是要保存文件
                if (isRecord || isSaveFile) {
                    filePath = Environment.getExternalStorageDirectory() + "";
                    audioFile = FileUtils.createFile(filePath, DateUitls.getNowTimeFileName(recordTime)+".mp4");
                    //开通输出流到指定的文件
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                }

                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = android.media.AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                int bufferSizeTrack = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
                Log.e("gufl", " bufferSize: " + bufferSize + ",bufferSizeTrack" + bufferSizeTrack);
                //实例化AudioRecord
                android.media.AudioRecord record = new android.media.AudioRecord(MediaRecorder.AudioSource.MIC, frequence, channelConfig, audioEncoding, bufferSize);
                //android.media.AudioRecord record = findAudioRecord();
                //定义缓冲
                short[] buffer = new short[bufferSize / 4];

                //定义输入流，将音频写入到AudioTrack类中，实现播放
                //DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));

                AudioTrack track = null;
                try {
                    //开始录制
                    record.startRecording();
                    if (!isRecord) {
                        //实例AudioTrack
                        track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, bufferSizeTrack, AudioTrack.MODE_STREAM);
                        //开始播放
                        track.play();
                    }

                    Log.e("gufl", " intervalTime: " + recordTime);

                    //定义循环，根据isRecording的值来判断是否继续录制
                    while (isRecording) {


                        //从bufferSize中读取字节，返回读取的short个数
                        //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                        int bufferReadResult = record.read(buffer, 0, buffer.length);

                        if (null != track) {
                            //然后将数据写入到AudioTrack中
                            track.write(buffer, 0, buffer.length);
                        }

                        if (isRecord || isSaveFile) {
                            //循环将buffer中的音频数据写入到OutputStream中
                            for (int i = 0; i < bufferReadResult; i++) {
                                dos.writeShort(buffer[i]);
                                //list.add(buffer[i]);
                            }
                        }

                        long intervalTime = System.currentTimeMillis() - recordTime;
                        publishProgress(intervalTime); //向UI线程报告当前进度
                    }
                } catch (IllegalStateException ex) {
                    Log.e("gufl", " startRecording IllegalStateException");
                    ex.printStackTrace();
                }

                //录制结束
                record.stop();
                record.release();

                //释放播放
                if (null != track) {
                    track.stop();
                    track.release();
                }

                //释放文件写入
                if(null!=dos){
                    dos.close();
                }

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            return null;
        }

        //当在上面方法中调用publishProgress时，该方法触发,该方法在UI线程中被执行
        protected void onProgressUpdate(Long... progress) {
            //stateView.setText(progress[0].toString());
            progressLisenter.onProgressUp(progress[0]);
        }

        protected void onPostExecute(Void result) {
            Log.e("gufl", " onPostExecute ");
/*            btnStop.setEnabled(false);
            btnStart.setEnabled(true);
            btnPlay.setEnabled(true);
            btnFinish.setEnabled(false);*/
        }

        protected void onPreExecute() {
            Log.e("gufl", " 正在录制 ");
            //stateView.setText("正在录制");
/*            btnStart.setEnabled(false);
            btnPlay.setEnabled(false);
            btnFinish.setEnabled(false);
            btnStop.setEnabled(true);*/
        }

    }

    class PlayTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            Log.e("gufl", " PlayTask -----doInBackground-----");
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
            Log.e("gufl", " bufferSize:" + bufferSize);
            short[] buffer = new short[bufferSize];
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                // DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                // Log.e("gufl", "dis.available() :" + dis.available());
/*                while (!(dis.available() > 0)) {
                    Thread.sleep(500);
                    Log.e("gufl", "wait 500 mill ");
                }*/

                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                while (isPlaying) {
                    int i = 0;
                    while (i < buffer.length && list.size() > 0) {
                        Log.e("gufl", " 1 list size is :" + list.size());
                        Log.e("gufl", " 1 list.get(0) is :" + list.get(0));
                        buffer[i] = list.get(0);
                        list.remove(0);
                        Log.e("gufl", " 2 list size is :" + list.size());
                        Log.e("gufl", " 2 list.get(0) is :" + list.get(0));
                        Log.e("gufl", " read buffer is :" + buffer[i]);
                        i++;
                    }
                    //然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);

                }

                //播放结束
                track.stop();
                // dis.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                Log.e("gufl", "---- PlayTask -has Exception -----" + e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.e("gufl", "-----doInBackground--onPostExecute---");
/*            btnPlay.setEnabled(true);
            btnFinish.setEnabled(false);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);*/
        }

        protected void onPreExecute() {
            Log.e("gufl", "-----doInBackground--onPreExecute---");
            //stateView.setText("正在播放");
/*            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
            btnPlay.setEnabled(false);
            btnFinish.setEnabled(true);*/
        }

    }

    private static int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};

    public android.media.AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        Log.d("gufl", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = android.media.AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != android.media.AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            android.media.AudioRecord recorder = new android.media.AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
                            Log.d("gufl", "final ----->Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig + ",bufferSize:" + bufferSize);
                            if (recorder.getState() == android.media.AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("gufl", rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 更新录制进度
     **/
    public interface OnRecordProgressLisenter {
        public void onProgressUp(long progress);
    }
}
