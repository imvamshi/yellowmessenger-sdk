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
        props.put("account","bajaj-finserv-bots");
        props.put("country","IN");
        YellowMessenger.init(this.getApplicationContext(),props);
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }
}