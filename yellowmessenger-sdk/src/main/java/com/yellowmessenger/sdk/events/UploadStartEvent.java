package com.yellowmessenger.sdk.events;


import com.yellowmessenger.sdk.models.db.ChatMessage;

public class UploadStartEvent {
    private ChatMessage chatMessage;
    private String uploadId;

    public UploadStartEvent(ChatMessage chatMessage, String uploadId) {
        this.chatMessage = chatMessage;
        this.uploadId = uploadId;
    }

    public UploadStartEvent(String uploadId) {
        this.uploadId = uploadId;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
