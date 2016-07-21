package com.yellowmessenger.sdk.events;

/**
 * Created by kishore on 06/02/16.
 */
public class UploadCompleteEvent {
    String uploadId;

    public UploadCompleteEvent(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
