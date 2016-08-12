package com.yellowmessenger.sdk.events;

public class LoginEvent {
    private boolean close = false;

    public LoginEvent() {
    }

    public LoginEvent(boolean close) {
        this.close = close;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }
}
