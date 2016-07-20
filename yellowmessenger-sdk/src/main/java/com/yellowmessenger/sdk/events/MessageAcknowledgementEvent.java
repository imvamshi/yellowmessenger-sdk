package com.yellowmessenger.sdk.events;

import com.yellowmessenger.sdk.models.db.ChatMessage;

/**
 * Created by kishore on 19/05/16.
 */
public class MessageAcknowledgementEvent {
    private ChatMessage chatMessage;
    private boolean acknowledged;

    public MessageAcknowledgementEvent(ChatMessage chatMessage, boolean acknowledged) {
        this.chatMessage = chatMessage;
        this.acknowledged = acknowledged;
    }


    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
