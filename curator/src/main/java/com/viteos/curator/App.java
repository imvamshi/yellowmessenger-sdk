package com.viteos.curator;

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
        props.put("account","bot_1532580318855");
        props.put("name","Curator");
        props.put ("authorizationToken","5b5951de3d9328f7e4debe34");
        // props.put("audio","false");
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