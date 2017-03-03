package com.yellowmessenger.sdk.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


/**
 * Created by rashid on 02/03/17.
 */

public class YMFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("Received a Notification");
        if (remoteMessage.getNotification() != null) {
            notifcationHandler(remoteMessage);
        }

        if (remoteMessage.getData() != null) {
            dataHandler(remoteMessage);
        }
    }

    private void notifcationHandler (RemoteMessage remoteMessage) {
        // TODO: Implement for Notifications (Not required at the point of writing the SDK"
    }

    private void dataHandler (RemoteMessage remoteMessage) {
        Map <String, String> data = remoteMessage.getData();
        if (data.containsKey("type") && data.get("type").equalsIgnoreCase("com.yellowmessenger.sdk.NEW_MESSAGE")) {
            startXMPPService();
        }
    }

    public void startXMPPService() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Intent startServiceIntent = new Intent(getApplicationContext(), XMPPService.class);
                getApplication().startService(startServiceIntent);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
