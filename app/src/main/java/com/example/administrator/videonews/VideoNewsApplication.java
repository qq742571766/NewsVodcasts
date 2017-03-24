package com.example.administrator.videonews;

import android.app.Application;

import com.example.administrator.videonews.commons.ToastUtils;

public class VideoNewsApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化吐丝工具类
        ToastUtils.init(this);
    }
}
