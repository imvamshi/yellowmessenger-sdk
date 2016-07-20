package com.yellowmessenger.sdk.models.db;

import android.graphics.Bitmap;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.Gson;
import com.yellowmessenger.sdk.models.ChatResponse;
import com.yellowmessenger.sdk.models.ChatType;

import java.text.SimpleDateFormat;
import java.util.Date;

@Table(name = "chat_message")
public class ChatMessage extends Model{
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm dd MMM, yyyy");

    @Column(name = "username",index = true)
    private String username;
    @Column(name = "message")
    private String message;

    @Column(name = "message_value")
    private String messageValue;

    @Column(name = "name")
    private String name;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "stanza_id")
    private String stanzaId;

    @Column(name = "you")
    private boolean you;

    @Column(name = "unsent",index = true)
    private Boolean unsent = false;

    @Column(name = "unread")
    private Boolean unread = false;
    @Column(name = "acknowledged")
    private Boolean acknowledged = false;


    private Bitmap bitmap;

    public ChatMessage() {
        super();
    }

    public ChatMessage(String username, String message, String name, boolean you) {
        super();
        this.username = username;
        this.message = message;
        this.name = name;
        this.you = you;
        this.timestamp = format.format(new Date());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isYou() {
        return you;
    }

    public void setYou(boolean you) {
        this.you = you;
    }

    public Boolean getUnsent() {
        return unsent;
    }

    public void setUnsent(Boolean unsent) {
        this.unsent = unsent;
    }

    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    public String getStanzaId() {
        return stanzaId;
    }

    public void setStanzaId(String stanzaId) {
        this.stanzaId = stanzaId;
    }



    private ChatResponse chatResponse;
    private ChatType chatType;
    private Gson gson = new Gson();

    public ChatType getChatType(){
        if(chatType == null){
            try{
                chatResponse = gson.fromJson(getMessage(), ChatResponse.class);
            }catch (Exception e){
                // e.printStackTrace();
            }

            if(chatResponse != null && chatResponse.getProduct()!=null){
                chatType = ChatType.PRODUCT;
            }else
            if(chatResponse != null && chatResponse.getSearchResults()!=null){
                chatType = ChatType.RESULTS;
            }else
            if(chatResponse != null && chatResponse.getQuestion()!=null){
                chatType = ChatType.QUESTION;
            }else
            if(chatResponse != null && chatResponse.getDeepLink()!=null){
                chatType = ChatType.DEEP_LINK;
            }else
            if(chatResponse != null && chatResponse.getTyping()!=null){
                chatType = ChatType.TYPING;
            }else
            if(chatResponse != null && chatResponse.getLocation()!=null)
            {
                chatType = ChatType.LOCATION;
            }else
            {
                chatType = ChatType.MESSAGE;
            }
        }
        return chatType;
    }

    public ChatResponse getChatResponse(){
        return chatResponse;
    }

    public int getViewType() {
        if(isYou()){
            if(getChatType()==ChatType.LOCATION){
                return 6;
            }
            return 0;
        }
        else
        if(getChatType()==ChatType.MESSAGE){
            return 1;
        }else
        if(getChatType()==ChatType.PRODUCT){
            return 2;
        }else
        if(getChatType() == ChatType.RESULTS && getChatResponse().getSearchResults().isPortraitImage())
        {
            return 3;
        }
        else
        if(getChatType() == ChatType.RESULTS && !getChatResponse().getSearchResults().isPortraitImage())
        {
            return 7;
        }
        else
        if(getChatType() == ChatType.QUESTION){
            return 4;
        }
        else
        if(getChatType() == ChatType.DEEP_LINK){
            return 5;
        }
        return 1;
    }
}
