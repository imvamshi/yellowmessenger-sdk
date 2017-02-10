package com.yellowmessenger.sampleapp;

import android.app.Application;

import com.yellowmessenger.sdk.service.YellowMessenger;

import java.util.HashMap;

/**
 * Created by kishore on 20/07/16.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HashMap<String,String> props = new HashMap<>();
        props.put("account","eca_bot");
        props.put("name","AP Field Tracker");
        YellowMessenger.init(this.getApplicationContext(),props);
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }
}