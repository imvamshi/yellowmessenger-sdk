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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.yellowmessenger.sdk.utils.NotificationUtil;
import com.yellowmessenger.sdk.utils.PreferencesManager;
import com.yellowmessenger.sdk.xmpp.CustomSCRAMSHA1Mechanism;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ExceptionCallback;
import org.jivesoftware.smack.ReconnectionManager;
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
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
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
    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
            10000,
            10,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    StanzaFilter packetFilter = new StanzaTypeFilter(Message.class);
    private String username;
    RequestQueue queue = null;

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
            try{
                Context context = getApplicationContext();
                PreferencesManager prefManager = PreferencesManager.getInstance(context);

                try{
                    String firebaseDeviceID = prefManager.getFirebaseDeviceID();
                    XMPPUser xmppUser = prefManager.getXMPPUser();
                    if (firebaseDeviceID != null && xmppUser != null) {
                        String authToken = PreferencesManager.getInstance(getApplicationContext()).getAuthorizationToken();
                        NotificationUtil.sendDeviceTokenToServer(firebaseDeviceID,xmppUser.getUsername(),authToken,getApplicationContext());
                    }
                }catch(Exception e){

                }

                mConnection.sendStanza(presence);
            }catch (Exception e){

            }
            sendUnsentMessages();
        }

        @Override
        public void connectionClosed() {
            try{
                if(!mConnection.isConnected() && isOnline()){
                    mConnection.connect();
                }
            }catch (Exception ex){
                //ex.printStackTrace();
            }
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
            Log.d(TAG,"Reconnecting in "+seconds);
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.d(TAG,"connection failed");
        }
    };

    StanzaListener packetListener = new StanzaListener() {
        @Override
        public void processStanza(Stanza stanza) throws SmackException.NotConnectedException, InterruptedException {
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
    StanzaFilter pingPacketFilter = new StanzaTypeFilter(Ping.class);


    StanzaListener pingPacketListener = new StanzaListener() {

        @Override
        public void processStanza(Stanza stanza) throws SmackException.NotConnectedException, InterruptedException {
            try{
                mConnection.sendStanza(((Ping) stanza).getPong());
            }catch (Exception e){

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
        String name = PreferencesManager.getInstance(getApplicationContext()).getBusinessName(sender);
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
                    Log.d("Event posting: ",message);
                    EventBus.getDefault().post(new TypingEvent(sender, chatResponse.getTyping()));
                }
            }

            if(chatResponse.isValid()){
                ChatMessage chatMessage = new ChatMessage(sender, message,sender, false);
                chatMessage.save();
                if (XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))
                {
                    Log.d("Event posting: ",message);
                    EventBus.getDefault().post(new MessageReceivedEvent(chatMessage));
                }
            }

            if (chatResponse.getTyping() == null && !(XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))) {
                notifyMessage(sender,name,"...");
            }
        }else {
            processXMPPMessage(sender, name, message);
        }
    }

    public void processXMPPMessage(String sender, String name, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message,name, false);
        chatMessage.save();
        if (XMPPService.this.username != null && sender.toLowerCase().equals(XMPPService.this.username.toLowerCase()))
        {
            Log.d("Event posting: ",message);
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
        queue = Volley.newRequestQueue(getApplicationContext());
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
            this.reconnect = false;
        }
        XMPPLoginAsyncTask(boolean close) {
            this.reconnect = close;
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
                    .setUsernameAndPassword(xmppUser.getUsername(),xmppUser.getPassword())
                    .build();

            SmackConfiguration.setDefaultPacketReplyTimeout(4000);
            XMPPTCPConnection.setUseStreamManagementDefault(true);
            XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);


            mConnection = new XMPPTCPConnection(connConfig);
            mConnection.setPacketReplyTimeout(4000);
            mConnection.setPreferredResumptionTime(5);

            mConnection.setUseStreamManagement(true);
            // mConnection.setUseStreamManagementResumption(true);
            mConnection.addAsyncStanzaListener(packetListener, packetFilter);
            mConnection.addAsyncStanzaListener(pingPacketListener, pingPacketFilter);
            mConnection.addStanzaAcknowledgedListener(new StanzaListener(){

                @Override
                public void processStanza(Stanza packet) throws SmackException.NotConnectedException {
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
            // ReconnectionManager.getInstanceFor(mConnection).enableAutomaticReconnection();
        }
        try{
            if (!mConnection.isConnected() && !mConnection.isAuthenticated()) {
                mConnection.connect();
            }else if(mConnection.isConnected() && !mConnection.isAuthenticated()){
                mConnection.login();
            }

        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    private void login() {
        if(!creatingUser ){
            XMPPUser xmppUser = PreferencesManager.getInstance(XMPPService.this.getApplicationContext()).getXMPPUser();
            if(xmppUser==null){
                anonymousUserLogin();
                return;
            }
        }else{
            return;
        }

        try {
            createConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyMessage(String sender,String name, String message){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle bundle = new Bundle();
        bundle.putString("username", sender);
        bundle.putString("name", name);
        intent.putExtras(bundle);
        //String customerName = PreferencesManager.getInstance(getApplicationContext()).getName()!=null?PreferencesManager.getInstance(getApplicationContext()).getName():"Yellow Messenger";
        sendNotification(name,message,intent);
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
        if (mConnection == null || !mConnection.isConnected() || !mConnection.isAuthenticated() || event.isClose()) {
            if (!connecting) {
                new XMPPLoginAsyncTask(event.isClose()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    private void anonymousUserLogin(){
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
            final XMPPTCPConnection anonymousConnection  = new XMPPTCPConnection(anonymousConfig);
            anonymousConnection.addConnectionListener(new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    try{
                        if(anonymousConnection.isConnected()){
                            anonymousConnection.login();
                        }
                    }catch (Exception e){
                        try{
                            if(anonymousConnection.getUser()!=null){
                                createUser(anonymousConnection.getUser().getLocalpart().toString());
                            }else{
                                anonymousConnection.connect();
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    createUser(anonymousConnection.getUser().getLocalpart().toString());
                    anonymousConnection.disconnect();
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


    class SendMessageAsyncTask extends AsyncTask<String, Void, Map> {

        SendMessageEvent event;

        SendMessageAsyncTask(SendMessageEvent event) {
            this.event = event;
        }

        @Override
        protected Map doInBackground(String... params) {

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
                Log.d(TAG,"Message not sent. Sending a login event");
            }else{
                Log.d(TAG,"Message sent");
                // Look for acknowledgement and disconnect if possible
            }
            Map<String,Object> map = new HashMap<>();
            map.put("unsent",unsent);
            map.put("stanzaId",chatMessage.getStanzaId());
            return map;
        }

        @Override
        protected void onPostExecute(Map map) {
            if(!(Boolean)map.get("unsent")){
                new Handler().postDelayed(new AckRunnable((String)map.get("stanzaId")),5000);
            }
        }
    }

    private class AckRunnable implements Runnable{
        String stanzaId;

        public AckRunnable(String stanzaId) {
            this.stanzaId = stanzaId;
        }

        @Override
        public void run() {
            ChatMessage chatMessage = ChatMessageDAO.getChatMessageByStanzaId(stanzaId);
            if(chatMessage !=null && !chatMessage.getAcknowledged() && isOnline()){
                if(!connecting){
                    EventBus.getDefault().post(new LoginEvent(true));
                }
            }
        }
    }
    boolean creatingUser = false;
    private void createUser(final String username){
        try{
            creatingUser = true;
            String salt = "a04aa6a74e76bf8f57b0e2e715138171";
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((username+salt).getBytes());
            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            String hash = sb.toString();

            String url = "https://sso.botplatform.io/createUser";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username",username);
            jsonObject.put("hash",hash);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success") && PreferencesManager.getInstance(getBaseContext()).getXMPPUser()==null) {
                                    JSONObject data = response.getJSONObject("data");
                                    PreferencesManager.getInstance(getBaseContext()).setXMPPUser(new XMPPUser(data.getString("username"),data.getString("password")));
                                    creatingUser = false;
                                    XMPPService.this.login();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });

            jsonObjectRequest.setRetryPolicy(retryPolicy);
            if(PreferencesManager.getInstance(getBaseContext()).getXMPPUser()==null){
                queue.add(jsonObjectRequest);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        startService(new Intent(getApplicationContext(), XMPPService.class));
    }
}
