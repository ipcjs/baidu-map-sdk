package com.baidu.lbsapi.panodemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;

public class PanoDemoApplication extends Application {

    private static PanoDemoApplication mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static PanoDemoApplication getInstance() {
        return mInstance;
    }
}