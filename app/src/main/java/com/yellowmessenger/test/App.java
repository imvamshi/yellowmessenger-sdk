package com.yellowmessenger.test;

import android.app.Application;

import com.yellowmessenger.sdk.service.YellowMessenger;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        YellowMessenger.init(this.getApplicationContext());
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }
}
