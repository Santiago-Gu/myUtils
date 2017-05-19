package com.hyc.monitor.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import com.gufeilong.androidutilslib.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Gu Feilong on 17-2-13.
 */

public class BitmapUtils {

    /**
     * 把图片压缩到指定大小，并返回base64编码
     * @param bitmap 原bitmap
     * @param newWidth 新宽度
     * @param newHeight 新高度
     * @return base64编码过后的图片，如果为空则说明问题
     */
    public static String bitmapToBase64(Bitmap bitmap, double newWidth,double newHeight){
        Bitmap tempBm = zoomImage(bitmap,newWidth,newHeight);
        String res = "";
        try{
            res = bitmapToBase64(tempBm);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bitmap.recycle();
            tempBm.recycle();
        }

        if(null==res){
            res = "";
        }


        return res;
    }
    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 把base64转为文件存储在本地
     * @param fileSavePath 文件保存到路径
     * @param fileName 文件保存的名字
     * @param base64Data 图片把se
     */
    public static boolean saveBitmap(String fileSavePath,String fileName,String base64Data){
        boolean res = false;
        OutputStream fos = null;
        FileUtils fileUtils = new FileUtils();
        Bitmap btmap = base64ToBitmap(base64Data);
        try {

            // 创建文件夹
            fileUtils.createSDDir(fileSavePath);
            // 创建文件
            File file = fileUtils.createSDFile(fileSavePath + fileName);
            fos = new FileOutputStream(file);
            res = btmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            res = false;
        } catch (IOException ex){
            ex.printStackTrace();
            res = false;
        }catch (Exception ex){
            ex.printStackTrace();
            res = false;
        }finally {

        }

        return res;
    }
    /***
     * 图片的缩放方法
     *
     * @param bgimage
     *            ：源图片资源
     * @param newWidth
     *            ：缩放后宽度
     * @param newHeight
     *            ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }
}
