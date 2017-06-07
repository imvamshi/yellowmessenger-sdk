package com.yellowmessenger.sdk.events;

/**
 * Created by kishore on 06/02/16.
 */
public class AudioCompleteEvent {
    String response;

    public AudioCompleteEvent(String response) {
        this.response = response;
    }


    public String getResponse() {
        return response;
    }
}
