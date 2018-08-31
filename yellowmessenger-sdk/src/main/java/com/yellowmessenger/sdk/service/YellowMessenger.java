package com.yellowmessenger.sdk.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.activeandroid.ActiveAndroid;
import com.yellowmessenger.sdk.utils.FontsOverride;
import com.yellowmessenger.sdk.utils.PreferencesManager;

import java.util.HashMap;

public class YellowMessenger {
    private Context _context;
    private static YellowMessenger instance;

    private YellowMessenger(Context context){
        this._context = context;
    }

    public static void init(Context context, HashMap<String,String> properties){
        getInstance(context);
        try{
            // Start the xmpp service
            instance.startXMPPService();
            instance._context = context;
            ActiveAndroid.initialize(context);
            PreferencesManager.getInstance(context).setAccountProperties(properties);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void terminate(){

    }

    private static YellowMessenger getInstance(Context context){
        if(instance==null){
            instance = new YellowMessenger(context);
            FontsOverride.setDefaultFont(context, "SERIF", "fonts/Lato-Regular.ttf");
        }
        return instance;
    }

    private void startXMPPService() {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Intent startServiceIntent = new Intent(_context, XMPPService.class);
                        _context.startService(startServiceIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
