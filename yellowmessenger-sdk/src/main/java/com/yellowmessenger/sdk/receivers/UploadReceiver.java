package com.yellowmessenger.sdk.receivers;
import com.yellowmessenger.sdk.events.UploadCompleteEvent;
import com.yellowmessenger.sdk.upload.ServerResponse;
import com.yellowmessenger.sdk.upload.UploadInfo;
import com.yellowmessenger.sdk.upload.UploadServiceBroadcastReceiver;


import org.greenrobot.eventbus.EventBus;

public class UploadReceiver extends UploadServiceBroadcastReceiver {

    @Override
    public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
        EventBus.getDefault().post(new UploadCompleteEvent(uploadInfo.getUploadId()));
    }
}
