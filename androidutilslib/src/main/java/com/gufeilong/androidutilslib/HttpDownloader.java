package com.gufeilong.androidutilslib;


import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载工具类
 */
public class HttpDownloader {

    private String SDPATH;
    private URL url = null;
    public static final int DOWNLOADSUC = 0;
    public static final int DOWNLOADFAIL = -1;

    private DownloadProgressLisenter progressLisenter;
    private DownloadFinishLisenter finishLisenter;

    public HttpDownloader() {
        //得到当前外部存储设备的目录( /SDCARD )
        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    /**
     * 根据URL下载文件,前提是这个文件当中的内容是文本,函数的返回值就是文本当中的内容
     * 1.创建一个URL对象
     * 2.通过URL对象,创建一个HttpURLConnection对象
     * 3.得到InputStream
     * 4.从InputStream当中读取数据
     *
     * @param urlStr
     * @return
     */
    public String download(String urlStr) {
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader buffer = null;
        try {
            url = new URL(urlStr);
            //根据URL取得与资源提供的服务器的连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            //将连接流管道成BufferedReader
            buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            //利用BufferedReader逐行读取文本信息,并添加到StringBuffer中
            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (buffer != null) {
                    buffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将读取的文本信息以String的形式输出
        return sb.toString();
    }

    /**
     * @param urlStr   文件地址
     * @param path     文件保存路径
     * @param fileName 文件名
     * @return 文件的绝对路径
     */
    public String downFile(String urlStr, String path, String fileName) {

        InputStream inputStream = null;
        String filePath = null;

        try {
            FileUtils fileUtils = new FileUtils();
            //判断文件是否存在
            if (fileUtils.isSDFileExist(path + fileName)) {
                System.out.println("exits");
                filePath = SDPATH + path + fileName;
            } else {
                //得到io流
                inputStream = getInputStreamFromURL(urlStr);
                //从input流中将文件写入SD卡中
                File resultFile = fileUtils.write2SDFromInput(path, fileName, inputStream);
                if (resultFile != null) {

                    filePath = resultFile.getPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     */
    public InputStream getInputStreamFromURL(String urlStr) {

        HttpURLConnection urlConn;
        InputStream inputStream = null;
        try {
            url = new URL(urlStr);
            urlConn = (HttpURLConnection) url.openConnection();
            inputStream = urlConn.getInputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    /**
     * 启动下载任务，得到进度和下载结果需注册监听事件 DownloadProgressLisenter 和 DownloadFinishLisenter
     * @param url 下载文件的服务器路径
     * @param savePath 下载文件保存在SD卡上到路径
     * @param name 下载文件保存到名称
     */
    public void startDownLoadTask(String url,String savePath,String name){
        DownloadTask downloadTask = new DownloadTask(url,savePath,name);
        downloadTask.execute(url);
    }

    class DownloadTask extends AsyncTask<String, Integer, Integer> {

        private String fileUrl,fileName,fileSavePath;

        public DownloadTask(String url,String savePath,String name){
            super();
            fileUrl = url;
            fileName = name;
            fileSavePath = savePath;
        }

        /**
         * 这里的String参数对应AsyncTask中的第一个参数
         * 这里的Integer返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected Integer doInBackground(String... params) {

            //每次读取的输入流的大小
            int FILESIZE = 10 * 1024;
            int res = DOWNLOADFAIL;
            InputStream inputStream = null;
            OutputStream output = null;
            try {
                FileUtils fileUtils = new FileUtils();
                // 创建文件夹
                fileUtils.createSDDir(fileSavePath);
                // 创建文件
                File file = fileUtils.createSDFile(fileSavePath + fileName);

                URL url = new URL(fileUrl);
                URLConnection conn = url.openConnection();
                // 建立链接
                conn.connect();
                // 获取输入流
                inputStream = conn.getInputStream();
                // 获取文件流大小，用于更新进度
                long file_length = conn.getContentLength();
                int total_length = 0;
                // 开启输出流，准备写入文件
                output = new FileOutputStream(file);
                // 缓冲区
                byte[] buffer = new byte[FILESIZE];
                int count;
                while ((count = inputStream.read(buffer)) != -1) {
                    int value = (int) ((total_length / (float) file_length) * 100);
                    // 调用update函数，更新进度
                    publishProgress(value);
                    output.write(buffer, 0, count);
                }
                output.flush();
                res = DOWNLOADSUC;
            } catch (Exception e) {
                e.printStackTrace();
                res = DOWNLOADFAIL;
            } finally {
                try {
                    output.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    res = DOWNLOADFAIL;
                }
            }

            return res;
        }


        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(Integer result) {
            if(null==finishLisenter){
                return;
            }

            if(DOWNLOADSUC == result){
                finishLisenter.downloadFinish(DOWNLOADSUC);
            }else if(DOWNLOADFAIL == result){
                finishLisenter.downloadFinish(DOWNLOADFAIL);
            }else{

            }
        }


        /**
         * 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
         */

        @Override
        protected void onPreExecute() {
            //textView.setText("开始执行异步线程");

        }


        /**
         * 这里的Intege参数对应AsyncTask中的第二个参数
         * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
         * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            int vlaue = values[0];
            if(null==progressLisenter){
                return;
            }
                progressLisenter.updateProgress(vlaue);
        }

    }

    /**
     * 设置下载进度监听
     * @param lisenter 监听事件
     */
    public void setDownProgressLisenter(DownloadProgressLisenter lisenter){
        this.progressLisenter = lisenter;
    }

    /**
     * 设置下载完成监听
     * @param lisenter 下载完成成功或失败监听
     */
    public void setDownloadFinishLisenter(DownloadFinishLisenter lisenter){
        this.finishLisenter = lisenter;
    }

    /**
     * 更新进度监听
     */
    public interface DownloadProgressLisenter{
        /**
         * 返回下载进度
         * @param progress 返回百分比结果
         */
        public void updateProgress(int progress);
    };

    /**
     * 下载完成监听
     */
    public interface DownloadFinishLisenter{
        /**
         * 返回下载完成结果
         * @param res  成功 DOWNLOADSUC = 0; 失败 DOWNLOADFAIL = -1;
         */
        public void downloadFinish(int res);
    }

}
