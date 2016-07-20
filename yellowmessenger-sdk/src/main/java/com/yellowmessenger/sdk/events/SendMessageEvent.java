package com.yellowmessenger.sdk.events;

import com.yellowmessenger.sdk.models.db.ChatMessage;

public class SendMessageEvent {
    private ChatMessage chatMessage;

    public SendMessageEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
