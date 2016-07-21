package com.yellowmessenger.sdk.events;

import com.yellowmessenger.sdk.models.Action;

public class SendActionEvent {
    private Action action;

    public SendActionEvent(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
