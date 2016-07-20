package com.yellowmessenger.sdk.events;

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
