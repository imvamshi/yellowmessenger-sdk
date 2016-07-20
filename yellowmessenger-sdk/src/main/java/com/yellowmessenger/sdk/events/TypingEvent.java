package com.yellowmessenger.sdk.events;

public class TypingEvent {
    private String sender;
    private Boolean typing;

    public TypingEvent(String sender, Boolean typing) {
        this.sender = sender;
        this.typing = typing;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }
}
