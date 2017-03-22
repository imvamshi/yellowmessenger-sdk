package com.yellowmessenger.sampleapp;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

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
        props.put ("authorizationToken","589617d3642aab705b255d72");
        YellowMessenger.init(this.getApplicationContext(),props);
    }

    @Override
    public void onTerminate() {
        YellowMessenger.terminate();
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}