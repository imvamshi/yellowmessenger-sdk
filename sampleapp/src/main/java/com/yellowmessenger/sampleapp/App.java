package com.yellowmessenger.sampleapp;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.yellowmessenger.sdk.service.YellowMessenger;

import java.util.HashMap;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HashMap<String,String> props = new HashMap<>();
        props.put("account","bot_1495864518793");
        props.put("name","Emirates NBD");
        props.put ("authorizationToken","592914c643e65e7cc5417384");
        props.put("audio","true");
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