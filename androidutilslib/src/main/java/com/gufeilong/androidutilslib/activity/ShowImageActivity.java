package com.gufeilong.androidutilslib.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gufeilong.androidutilslib.FileUtils;
import com.gufeilong.androidutilslib.HttpDownloader;


/**
 * * 用来显示网络图片
 * 1、默认显示缩略图;
 * 2、点击可自动加载服务器图片;
 * Created by Gu Feilong on 17-2-15.
 */

public class ShowImageActivity extends Activity implements HttpDownloader.DownloadProgressLisenter, HttpDownloader.DownloadFinishLisenter {


    private ProgressBar loadProBar;
    private ImageView bigImageView;
    /**
     * 缩略图本地路径
     **/
    private String thumPath;
    /** 文件名称 （根据文件名判断是否已经存在）**/
    private String picName;
    /**
     * 网络图片路径
     **/
    private String serverPicPath;
    /**
     * 文件下载器
     **/
    private HttpDownloader downloader;
    /** 文件操作工具类**/
    private FileUtils fileUtils;

    private String savePath = "Downloagimage/";

    public static final String THUMTAG = "thumpath";
    public static final String PICNAMETAG = "picName";
    public static final String SAVEPATHTAG = "savePathTAG";
    public static final String SERVERPICTAG = "serverPicPath";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_showimage);
        mContext = this;
        downloader = new HttpDownloader();
        downloader.setDownloadFinishLisenter(this);
        downloader.setDownProgressLisenter(this);
        fileUtils = new FileUtils();

        initView();
        getIntentData();

    }

    /**
     * 初始化视图
     */
    private void initView() {
        bigImageView = (ImageView) findViewById(R.id.big_iv);
        loadProBar = (ProgressBar)findViewById(R.id.load_probar);
    }

    /**
     * 获取从界面传入的图片路径数据
     */
    private void getIntentData() {
        Intent dataIn = getIntent();
        thumPath = dataIn.getStringExtra(THUMTAG);
        serverPicPath = dataIn.getStringExtra(SERVERPICTAG);
        picName = dataIn.getStringExtra(PICNAMETAG);
        if(!TextUtils.isEmpty(dataIn.getStringExtra(SAVEPATHTAG))){
            savePath = dataIn.getStringExtra(SAVEPATHTAG);
        }

        if (!TextUtils.isEmpty(thumPath)) {
            Uri imagetUri = Uri.parse(thumPath);
            bigImageView.setImageURI(imagetUri);
        }
        //当图片名为空时，获取URL最后字段作为文件名
        if(TextUtils.isEmpty(picName)&&!TextUtils.isEmpty(serverPicPath)){
            picName = serverPicPath.substring(serverPicPath.lastIndexOf("/")+1,serverPicPath.length());
        }

    }




    @Override
    protected void onStart() {
        super.onStart();
        if(fileUtils.isSDFileExist(savePath+picName)){
            loadProBar.setVisibility(View.GONE);
            bigImageView.setImageURI(Uri.parse(FileUtils.SDPATH+savePath+picName));
        }else if (!TextUtils.isEmpty(serverPicPath)) {
            downloader.startDownLoadTask(serverPicPath, savePath, picName);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void finishView(View v) {
        finishAfterTransition();
    }

    @Override
    public void downloadFinish(int res) {
        loadProBar.setVisibility(View.GONE);
        if (HttpDownloader.DOWNLOADSUC == res) {
            bigImageView.setImageURI(Uri.parse(FileUtils.SDPATH+savePath + picName));
        } else if (HttpDownloader.DOWNLOADFAIL == res) {
            Toast.makeText(mContext, "文件下载失败", Toast.LENGTH_LONG).show();
        } else {

        }
    }

    @Override
    public void updateProgress(int progress) {

    }
}
