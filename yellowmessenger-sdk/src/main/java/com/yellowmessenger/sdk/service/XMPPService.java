package com.yellowmessenger.sdk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.yellowmessenger.sdk.ChatActivity;
import com.yellowmessenger.sdk.R;
import com.yellowmessenger.sdk.dao.ChatMessageDAO;
import com.yellowmessenger.sdk.events.ChatConnectedEvent;
import com.yellowmessenger.sdk.events.ChatDisconnectedEvent;
import com.yellowmessenger.sdk.events.LoginEvent;
import com.yellowmessenger.sdk.events.MessageAcknowledgementEvent;
import com.yellowmessenger.sdk.events.MessageReceivedEvent;
import com.yellowmessenger.sdk.events.NetworkChangeEvent;
import com.yellowmessenger.sdk.events.SendMessageEvent;
import com.yellowmessenger.sdk.events.TypingEvent;
import com.yellowmessenger.sdk.events.UploadCompleteEvent;
import com.yellowmessenger.sdk.events.UploadStartEvent;
import com.yellowmessenger.sdk.models.ChatResponse;
import com.yellowmessenger.sdk.models.XMPPUser;
import com.yellowmessenger.sdk.models.db.ChatMessage;
import com.yellowmessenger.sdk.receivers.UploadReceiver;
import com.yellowmessenger.sdk.utils.PreferencesManager;
import com.yellowmessenger.sdk.xmpp.CustomSCRAMSHA1Mechanism;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.extensions.ExtensionsInitializer;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class XMPPService extends Service {
    private static final String TAG = "Yellow Messenger - ";
    public static final String HOST = "xmpp.yellowmssngr.com";
    public static final int PORT = 443;
    public static final String DOMAIN = "xmpp.yellowmssngr.com";
    private Presence presence = null;
    boolean clientIsBound = false;
    private final IBinder mBinder = new LocalBinder();
    boolean mAllowRebind = true;
    boolean connecting = false;
    private UploadReceiver uploadReceiver = new UploadReceiver();
    private static XMPPTCPConnection mConnection;

    StanzaFilter packetFilter = new StanzaTypeFilter(Message.class);
    private String username;
    Gson gson = new Gson();

    // Sub classes
    private class LocalBinder extends Binder {
        public XMPPService getService() {
            return XMPPService.this;
        }
    }

    ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            try {
                mConnection.login();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            // Add the jid to the cache
            Log.d(TAG, "authenticated: ");
            sendUnsentMessages();
        }

        @Override
        public void connectionClosed() {
            Log.d(TAG,"connection closed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            try{
                if(!mConnection.isConnected() && isOnline()){
                    mConnection.connect();
                }
            }catch (Exception ex){
                //ex.printStackTrace();
            }
            Log.d(TAG,"connection failed on error");
        }

        @Override
        public void reconnectionSuccessful() {
            try {

                XMPPService.this.connecting = false;
                mConnection.login();
                // TODO
                // sendUnsentMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void reconnectingIn(int seconds) {
            XMPPService.this.connecting = true;
            Log.d(TAG,"Reconnecting...");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.d(TAG,"connection failed");
        }
    };

    StanzaListener packetListener = new StanzaListener() {
        @Override
        public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
            if (stanza instanceof Message) {
                Message message = (Message) stanza;
                Jid sender = message.getFrom();
                if (message.getBody() != null && message.getType().equals(Message.Type.chat)) {
                    // Do some stuff here
                    Log.d(TAG,"Received message: "+message.getBody());
                    if(sender.getLocalpartOrNull()!=null){
                        processMessage(sender.getLocalpartOrNull().toString(),message.getBody());
                    }
                }
            }
        }
    };

    public void sendUnsentMessages() {
        try {
            List<ChatMessage> unsentMessages = ChatMessageDAO.getUnsentMessages();

            for (ChatMessage chatMessage : unsentMessages) {
                Message msg = new Message(JidCreate.from(chatMessage.getUsername(),DOMAIN,""), Message.Type.chat);
                if(chatMessage.getMessageValue()!=null){
                    msg.setBody(chatMessage.getMessageValue());
                }else{
                    msg.setBody(chatMessage.getMessage());
                }
                mConnection.sendStanza(msg);
                chatMessage.setUnsent(false);
                chatMessage.setStanzaId(msg.getStanzaId());
                chatMessage.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(String sender,String message){
        ChatResponse chatResponse = null;
        try{
            chatResponse = gson.fromJson(message, ChatResponse.class);
        }catch (Exception e){
            // e.printStackTrace();
        }

        if (chatResponse != null) {
            if(chatResponse.getTyping()!=null){
                if (XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))
                {
                    EventBus.getDefault().post(new TypingEvent(sender, chatResponse.getTyping()));
                }
            }

            if(chatResponse.isValid()){
                ChatMessage chatMessage = new ChatMessage(sender, message,sender, false);
                chatMessage.save();
                if (XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))
                {
                    EventBus.getDefault().post(new MessageReceivedEvent(chatMessage));
                }
            }

            if (chatResponse.getTyping() == null && !(XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))) {
                notifyMessage(sender,sender,"...");
            }
        }else {
            processXMPPMessage(sender, sender, message);
        }
    }

    public void processXMPPMessage(String sender, String name, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message,name, false);
        chatMessage.save();
        if (XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))
        {
            EventBus.getDefault().post(new MessageReceivedEvent(chatMessage));
        } else {
            notifyMessage(sender, name, message);
        }
    }

    @Override
    public void onCreate() {
        presence = new Presence(Presence.Type.available);
        presence.setStatus("I’m available");
        EventBus.getDefault().register(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        //login();
        if (!connecting) {
            new XMPPLoginAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        uploadReceiver.register(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        clientIsBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        clientIsBound = false;
        return mAllowRebind;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    class XMPPLoginAsyncTask extends AsyncTask<String, Void, Void> {
        boolean reconnect;

        XMPPLoginAsyncTask() {
            this.reconnect = reconnect;
        }

        @Override
        protected void onPreExecute() {
            connecting = true;
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.d(TAG, "Logging in...");
            XMPPService.this.login();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            connecting = false;
        }
    }

    private void createConnection() throws XmppStringprepException, NoSuchAlgorithmException {
        if (mConnection == null) {
            AndroidSmackInitializer androidSmackInitializer = new AndroidSmackInitializer();
            androidSmackInitializer.initialize();
            ExtensionsInitializer extensionsInitializer= new ExtensionsInitializer();
            extensionsInitializer.initialize();

            XMPPUser xmppUser = PreferencesManager.getInstance(XMPPService.this.getApplicationContext()).getXMPPUser();
            XMPPTCPConnectionConfiguration connConfig = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(JidCreate.domainBareFrom(DOMAIN))
                    .setHost(HOST)
                    .setPort(PORT)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                    .setCustomSSLContext(SSLContext.getInstance("TLS"))
                    .setSocketFactory(SSLSocketFactory.getDefault())
                    .setUsernameAndPassword(xmppUser.getUsername(),"")
                    .build();

            SmackConfiguration.setDefaultPacketReplyTimeout(60000);
            XMPPTCPConnection.setUseStreamManagementDefault(true);
            XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);


            mConnection = new XMPPTCPConnection(connConfig);
            mConnection.setPacketReplyTimeout(60000);
            mConnection.setPreferredResumptionTime(120);

            mConnection.setUseStreamManagement(true);
            mConnection.setUseStreamManagementResumption(true);
            mConnection.addAsyncStanzaListener(packetListener, packetFilter);
            mConnection.addStanzaAcknowledgedListener(new StanzaListener(){

                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    // TODO Acknowledgement
                    ChatMessage chatMessage = ChatMessageDAO.getChatMessageByStanzaId(packet.getStanzaId());
                    if(chatMessage!=null){
                        chatMessage.setAcknowledged(true);
                        chatMessage.save();
                        EventBus.getDefault().post(new MessageAcknowledgementEvent(chatMessage,chatMessage.getAcknowledged()));
                    }
                }
            });

            SASLAuthentication.unregisterSASLMechanism("org.jivesoftware.smack.sasl.core.SCRAMSHA1Mechanism");
            SASLAuthentication.registerSASLMechanism(new CustomSCRAMSHA1Mechanism());

            mConnection.addConnectionListener(connectionListener);
            ServerPingWithAlarmManager.getInstanceFor(mConnection).setEnabled(true);
        }
        try{
            if (!mConnection.isConnected() && !mConnection.isAuthenticated()) {
                mConnection.connect();
            }
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    private void login() {
        XMPPUser xmppUser = PreferencesManager.getInstance(XMPPService.this.getApplicationContext()).getXMPPUser();
        if(xmppUser==null){
            createUser();
            return;
        }
        try {
            createConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mConnection != null && (!mConnection.isConnected() || !mConnection.isAuthenticated())) {
            if (!mConnection.isConnected()) {
                try {
                    Log.d(TAG,"Reconnecting ...");
                    mConnection.connect();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private void notifyMessage(String sender,String name, String message){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle bundle = new Bundle();
        bundle.putString("username", sender);
        bundle.putString("name", name);
        intent.putExtras(bundle);
        sendNotification(PreferencesManager.getInstance(getApplicationContext()).getName()!=null?PreferencesManager.getInstance(getApplicationContext()).getName():"Yellow Messenger",message,intent);
    }

    private void sendNotification(String title, String message, Intent intent) {
        int requestID = (int) System.currentTimeMillis();
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), requestID, intent, 0);

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.notify_icon)
                //.setColor(getResources().getColor(R.color.action_bar_color))
                .setAutoCancel(true);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = builder.getNotification();
        n.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }

    @Subscribe
    public void onEvent(SendMessageEvent event) {
        new SendMessageAsyncTask(event).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    public void onEvent(ChatConnectedEvent event) {
        username = event.getUsername();
    }

    @Subscribe
    public void onEvent(ChatDisconnectedEvent event) {
        username = null;
    }

    Map<String,ChatMessage> uploadMap = new HashMap<>();

    @Subscribe
    public void onEvent(UploadCompleteEvent event) {
        ChatMessage chatMessage = uploadMap.get(event.getUploadId());
        chatMessage.setBitmap(null);
        EventBus.getDefault().post(new SendMessageEvent(chatMessage));
    }

    @Subscribe
    public void onEvent(UploadStartEvent event) {
        ChatMessage chatMessage = event.getChatMessage();
        uploadMap.put(event.getUploadId(),chatMessage);
    }


    @Subscribe
    public void onEvent(LoginEvent event) {
        Log.d(TAG,"Login event received");
        if (mConnection == null || !mConnection.isConnected() || !mConnection.isAuthenticated()) {
            if (!connecting) {
                new XMPPLoginAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Subscribe
    public void onEvent(NetworkChangeEvent event) {
        if (mConnection == null || !mConnection.isConnected() || !mConnection.isAuthenticated()) {
            if (!connecting && isOnline()) {
                new XMPPLoginAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    // Create an anonymous account
    private void createUser(){
        try{
            XMPPTCPConnectionConfiguration anonymousConfig = XMPPTCPConnectionConfiguration.builder()
                    .performSaslAnonymousAuthentication()
                    .setXmppDomain(JidCreate.domainBareFrom(DOMAIN))
                    .setHost(HOST)
                    .setPort(PORT)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                    .setCustomSSLContext(SSLContext.getInstance("TLS"))
                    .setSocketFactory(SSLSocketFactory.getDefault())
                    .build();
            final XMPPTCPConnection anonymousConnection = new XMPPTCPConnection(anonymousConfig);
            anonymousConnection.addConnectionListener(new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    try{
                        if(anonymousConnection.isConnected()){
                            anonymousConnection.login();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    PreferencesManager.getInstance(getBaseContext()).setXMPPUser(new XMPPUser(anonymousConnection.getUser().getLocalpart().toString(),""));
                    anonymousConnection.disconnect();
                    XMPPService.this.login();
                }

                @Override
                public void connectionClosed() {

                }

                @Override
                public void connectionClosedOnError(Exception e) {

                }

                @Override
                public void reconnectionSuccessful() {

                }

                @Override
                public void reconnectingIn(int seconds) {

                }

                @Override
                public void reconnectionFailed(Exception e) {

                }
            });
            anonymousConnection.connect();

        }catch (Exception e){

        }

    }


    class SendMessageAsyncTask extends AsyncTask<String, Void, Void> {

        SendMessageEvent event;

        SendMessageAsyncTask(SendMessageEvent event) {
            this.event = event;
        }

        @Override
        protected Void doInBackground(String... params) {

            boolean unsent = false;
            if(mConnection==null || !mConnection.isConnected() || !mConnection.isAuthenticated()){
                unsent = true;
            }

            if(mConnection!=null && mConnection.isDisconnectedButSmResumptionPossible()){
                Log.d(TAG,"Disconnected but resumption possible");
            }

            String stanzaId = null;

            if(!unsent){
                try {
                    Message msg = new Message(JidCreate.from(event.getChatMessage().getUsername(),DOMAIN,""), Message.Type.chat);
                    if(event.getChatMessage().getMessageValue()!=null){
                        msg.setBody(event.getChatMessage().getMessageValue());
                    }else{
                        msg.setBody(event.getChatMessage().getMessage());
                    }
                    DeliveryReceiptRequest.addTo(msg);

                    mConnection.sendStanza(msg);
                    stanzaId = msg.getStanzaId();
                } catch (Exception ex) {
                    unsent = true;
                    ex.printStackTrace();
                }
            }

            ChatMessage chatMessage = event.getChatMessage();
            chatMessage.setUnsent(unsent);
            chatMessage.setStanzaId(stanzaId);
            chatMessage.save();

            if(unsent){
                EventBus.getDefault().post(new LoginEvent());
            }
            return null;
        }
    }
}
