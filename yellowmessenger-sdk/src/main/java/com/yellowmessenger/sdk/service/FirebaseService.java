package com.yellowmessenger.sdk.service;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yellowmessenger.sdk.models.XMPPUser;
import com.yellowmessenger.sdk.utils.NotificationUtil;
import com.yellowmessenger.sdk.utils.PreferencesManager;

/**
 * Created by rashid on 02/03/17.
 */

public class FirebaseService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println(FirebaseInstanceId.getInstance().getId());

        // Save the Token in Preferences
        Context context = getApplicationContext();
        PreferencesManager prefManager = PreferencesManager.getInstance(context);

        prefManager.setFirebaseDeviceID(refreshedToken);
        XMPPUser xmppUser = prefManager.getXMPPUser();

        if (xmppUser != null && xmppUser.getUsername() != null) {
            String authToken = prefManager.getAuthorizationToken();
            if (authToken != null) {
                NotificationUtil.sendDeviceTokenToServer(refreshedToken, xmppUser.getUsername(), authToken, context);
            } else {
                System.out.println("AuthorizationToken is null -- FirebaseService.onTokenRefresh");
            }
        }
    }
}
