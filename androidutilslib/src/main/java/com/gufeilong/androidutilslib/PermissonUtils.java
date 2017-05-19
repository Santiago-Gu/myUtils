package com.gufeilong.androidutilslib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by santiago on 17-1-19.
 */
public class PermissonUtils {

    public static final int STORAGE_REQUEST_CODE = 102;
    public static final int AUDIO_REQUEST_CODE   = 103;

    /**
     * 测试当前摄像头能否被使用
     *
     * @return
     */
    public static boolean isCameraCanUse(Context context) {


        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", context.getPackageName()));
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            permission = false;
        }
        if (permission) {
            mCamera.release();
            mCamera = null;
        }

        return permission;
    }

    /** 请求向SD卡写文件权限 **/
    public static void checkWriteFilePermisson(Context context,Activity activity){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }
    }

    /** 请求录制音频权限 **/
    public static void checkRecordAudio(Context context,Activity activity){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
    }



}
