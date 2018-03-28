package com.yellowmessenger.sdk.events;


public class UploadCompleteEvent {
    String uploadId;
    String url;

    public UploadCompleteEvent(){

    }

    public UploadCompleteEvent(String uploadId, String url) {
        this.uploadId = uploadId;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
