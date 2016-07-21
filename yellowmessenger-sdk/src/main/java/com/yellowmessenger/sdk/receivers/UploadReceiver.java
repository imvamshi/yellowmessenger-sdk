package com.yellowmessenger.sdk.receivers;
import com.yellowmessenger.sdk.events.UploadCompleteEvent;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import org.greenrobot.eventbus.EventBus;

public class UploadReceiver extends UploadServiceBroadcastReceiver {

    @Override
    public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
        EventBus.getDefault().post(new UploadCompleteEvent(uploadInfo.getUploadId()));
    }
}
