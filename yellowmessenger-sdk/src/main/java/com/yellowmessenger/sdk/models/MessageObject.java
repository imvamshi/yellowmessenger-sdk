package com.yellowmessenger.sdk.models;

public class MessageObject {
    private String image;
    private String message;
    private String priceString;
    private String messageType;
    private String objectId;
    private String details;

    public MessageObject(){

    }

    public MessageObject(String image, String message, String priceString, String messageType, String objectId, String details) {
        this.image = image;
        this.message = message;
        this.priceString = priceString;
        this.messageType = messageType;
        this.objectId = objectId;
        this.details = details;
    }

    public String getImage() {
        return image;
    }

    public String getMessage() {
        return message;
    }

    public String getPriceString() {
        return priceString;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPriceString(String priceString) {
        this.priceString = priceString;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
