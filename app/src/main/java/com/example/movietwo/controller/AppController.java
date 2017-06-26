package com.example.movietwo.controller;


import android.app.Application;
import android.util.Log;

import com.example.movietwo.networking.DataManager;

public class AppController extends Application {

    private static final String TAG = AppController.class
            .getSimpleName();
    // Different Managers
    private DataManager mDataMan;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "App started");
        initApp();
    }

    private void initApp() {
        mDataMan = DataManager.getInstance(AppController.this);
        mDataMan.init();
    }


    public synchronized DataManager getDataManager() {
        return mDataMan;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mDataMan != null) {
            mDataMan.terminate();
        }
    }
}
