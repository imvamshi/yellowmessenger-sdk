package com.yellowmessenger.sdk.events;

public class ChatConnectedEvent {
    String username;
    public ChatConnectedEvent(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
