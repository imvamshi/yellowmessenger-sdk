package com.yellowmessenger.sdk.events;

public class ChatUpdatedEvent {
    private boolean chatUpdated = false;

    public ChatUpdatedEvent(boolean chatUpdated) {
        this.chatUpdated = chatUpdated;
    }

    public boolean isChatUpdated() {
        return chatUpdated;
    }

    public void setChatUpdated(boolean chatUpdated) {
        this.chatUpdated = chatUpdated;
    }
}
