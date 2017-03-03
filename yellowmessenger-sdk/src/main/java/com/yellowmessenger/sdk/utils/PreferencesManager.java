package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.yellowmessenger.sdk.models.XMPPUser;

import java.util.HashMap;

public class PreferencesManager {
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;
    private static final String PREFER_NAME = "ym_sdk_preferences";
    int PRIVATE_MODE = 0;


    private static PreferencesManager instance;
    private String name;

    public static PreferencesManager getInstance(Context context){
        if(instance == null){
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    private PreferencesManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setXMPPUser(XMPPUser user){
        editor.putString("xmpp-username", user.getUsername());
        editor.putString("xmpp-password", user.getPassword());
        editor.commit();
    }

    public XMPPUser getXMPPUser(){
        String username = pref.getString("xmpp-username", null);
        String password = pref.getString("xmpp-password", null);
        return username!=null?new XMPPUser(username,password):null;
    }

    public void setAccountProperties(HashMap<String,String> accountProperties){
        for(String key:accountProperties.keySet()){
            editor.putString("props-"+key, accountProperties.get(key));
        }
        editor.commit();
    }

    public void setBusinessName(String username,String name){
        editor.putString("name-"+username,name);
        editor.commit();
    }

    public String getBusinessName(String username){
        return pref.getString("name-"+username, username);
    }

    public void setFirebaseDeviceID (String deviceToken) {
        editor.putString("firebaseDeviceToken", deviceToken);
        editor.commit();
    }

    public String getFirebaseDeviceID () {
        return pref.getString("firebaseDeviceToken", null);
    }

    public String getAccount(){
        return pref.getString("props-account", null);
    }

    public String getCountry(){
        return pref.getString("props-country", null);
    }

    public String getName() {
        return pref.getString("props-name", null);
    }

    public String getAuthorizationToken() { return pref.getString("props-authorizationToken", null); }
}
