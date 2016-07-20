package com.yellowmessenger.sdk.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


import com.yellowmessenger.sdk.events.NetworkChangeEvent;

import org.greenrobot.eventbus.EventBus;

public class NetworkStateReceiver extends BroadcastReceiver
{
    private static final String DEBUG_TAG = "AlarmReceiver";
    private NetworkChangeEvent event = new NetworkChangeEvent();

    @Override
    public void onReceive( Context context, Intent intent )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if ( activeNetInfo != null && activeNetInfo.isConnected() )
        {
            Log.d(DEBUG_TAG, "Network change receiver");
            EventBus.getDefault().post(event);
        }
    }
}