package com.yellowmessenger.sdk.events;


import com.yellowmessenger.sdk.models.Option;

public class SendOptionEvent {
    private Option option;

    public SendOptionEvent(Option action) {
        this.option = action;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }
}
