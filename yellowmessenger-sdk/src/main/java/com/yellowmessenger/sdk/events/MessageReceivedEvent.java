package com.yellowmessenger.sdk.events;


import com.yellowmessenger.sdk.models.db.ChatMessage;

public class MessageReceivedEvent {
    private ChatMessage chatMessage;

    public MessageReceivedEvent(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
