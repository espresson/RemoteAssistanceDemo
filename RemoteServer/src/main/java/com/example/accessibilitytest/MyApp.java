package com.example.accessibilitytest;

import android.app.Application;
import android.app.smdt.SmdtManager;

import com.example.accessibilitytest.data.Const;


public class MyApp extends Application {
    private final String TAG = "MyApp";

    private static MyApp mApp;

    public  static MyApp getApp() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;

//        initSmdt();
    }

    private void initSmdt(){
        Const.smdtManager = SmdtManager.create(this);
        Const.smdtManager.smdtSetStatusBar(this,true);
        Const.smdtManager.setGestureBar(true);
        Const.smdtManager.setNetworkDebug(true);
    }
}
