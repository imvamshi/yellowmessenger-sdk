package com.yellowmessenger.sdk.events;

public class ChatDisconnectedEvent {
    String username;

    public ChatDisconnectedEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
