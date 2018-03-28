package com.yellowmessenger.sdk.receivers;

import com.google.gson.Gson;
import com.yellowmessenger.sdk.events.AudioCompleteEvent;
import com.yellowmessenger.sdk.events.UploadCompleteEvent;
import com.yellowmessenger.sdk.upload.ServerResponse;
import com.yellowmessenger.sdk.upload.UploadInfo;
import com.yellowmessenger.sdk.upload.UploadServiceBroadcastReceiver;

import org.greenrobot.eventbus.EventBus;

public class UploadReceiver extends UploadServiceBroadcastReceiver {

    @Override
    public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
        if(uploadInfo.getUploadId().equals("audio")){
            String body = new String(serverResponse.getBody());
            EventBus.getDefault().post(new AudioCompleteEvent(body));
        }else{
            UploadCompleteEvent body = new Gson().fromJson(new String(serverResponse.getBody()), UploadCompleteEvent.class );
            body.setUploadId(uploadInfo.getUploadId());
            EventBus.getDefault().post(body);
        }
    }
}
