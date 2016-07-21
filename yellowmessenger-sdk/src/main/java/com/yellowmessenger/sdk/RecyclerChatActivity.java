package com.yellowmessenger.sdk;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.yellowmessenger.sdk.dao.ChatMessageDAO;
import com.yellowmessenger.sdk.events.ChatConnectedEvent;
import com.yellowmessenger.sdk.events.ChatDisconnectedEvent;
import com.yellowmessenger.sdk.events.ChatUpdatedEvent;
import com.yellowmessenger.sdk.events.MessageAcknowledgementEvent;
import com.yellowmessenger.sdk.events.MessageReceivedEvent;
import com.yellowmessenger.sdk.events.SendActionEvent;
import com.yellowmessenger.sdk.events.SendMessageEvent;
import com.yellowmessenger.sdk.events.SendOptionEvent;
import com.yellowmessenger.sdk.events.UploadStartEvent;
import com.yellowmessenger.sdk.fragments.ProductFragment;
import com.yellowmessenger.sdk.models.ChatType;
import com.yellowmessenger.sdk.models.Option;
import com.yellowmessenger.sdk.models.Product;
import com.yellowmessenger.sdk.models.Question;
import com.yellowmessenger.sdk.models.db.ChatMessage;
import com.yellowmessenger.sdk.utils.ChatListAdapter;
import com.yellowmessenger.sdk.utils.PreferencesManager;
import com.yellowmessenger.sdk.utils.S3Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class RecyclerChatActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener {

    View.OnClickListener sendMessageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMessage(v);
        }
    };

    ChatListAdapter chatListAdapter;
    ListView listView;
    EditText editText;
    ViewGroup optionsLayout;
    View sendView;
    int padding10;
    int padding20;
    int margin5;
    int margin2;
    int size15;
    int elevation;
    boolean listViewMoving = false;

    public void sendMessage(View view) {
        String message = editText.getText().toString();
        if (!message.trim().equals("")) {
            ChatMessage chatMessage = new ChatMessage(username, message, name, true);
            EventBus.getDefault().post(new SendMessageEvent(chatMessage));
            addMessage(chatMessage);
            editText.setText("");
            optionsLayout.removeAllViews();
        }
    }

    public void addMessage(final ChatMessage chatMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatMessages.add(chatMessage);
                chatListAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_chat);
        EventBus.getDefault().register(this);
        editText = ((EditText) findViewById(R.id.sendText));
        chatMessages = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatMessages, name);
        listView = (ListView) findViewById(R.id.listView);
        sendView = findViewById(R.id.sendButton);
        padding10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        padding20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        margin5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        margin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        size15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 15, getResources().getDisplayMetrics());
        elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 2, getResources().getDisplayMetrics());
        optionsLayout = (ViewGroup) findViewById(R.id.optionsLayout);
        queue = Volley.newRequestQueue(RecyclerChatActivity.this);

        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this , this )
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        // set listeners
        setListeners();
        init();
    }

    private void setListeners() {
        listView.setAdapter(chatListAdapter);
        sendView.setOnClickListener(sendMessageListener);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (mLastFirstVisibleItem < firstVisibleItem) {
                    //Log.i("SCROLLING DOWN", "TRUE");
                }
                if (mLastFirstVisibleItem > firstVisibleItem && listViewMoving) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    @Subscribe
    public void onEvent(final MessageReceivedEvent event) {
        addMessage(event.getChatMessage());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private String username;
    private String name;
    private List<ChatMessage> chatMessages;

    private void init() {
        String oldUsername = username;
        username = getIntent().getExtras().getString("username");
        name = getIntent().getExtras().getString("name");
        setTitle(name);
        if (oldUsername == null || !username.equals(oldUsername)) {
            getChatHistory();
        }
    }

    private void getChatHistory() {
        chatMessages.clear();
        List<ChatMessage> newChatMessages = ChatMessageDAO.findAllByUsername(username, 100, 0);
        Collections.reverse(newChatMessages);
        chatMessages.addAll(newChatMessages);
        chatListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        EventBus.getDefault().post(new ChatConnectedEvent(username));
    }

    @Override
    public void onStop() {
        EventBus.getDefault().post(new ChatDisconnectedEvent(username));
        super.onStop();
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().post(new ChatDisconnectedEvent(username));
        super.onPause();
    }


    public void sendActionEvent(SendActionEvent sendActionEvent) {
        ChatMessage chatMessage = new ChatMessage(username, sendActionEvent.getAction().getTitle(), name, true);
        addMessage(chatMessage);
        chatMessage.setMessageValue(sendActionEvent.getAction().getText()!=null?sendActionEvent.getAction().getText():sendActionEvent.getAction().getTitle());
        EventBus.getDefault().post(new SendMessageEvent(chatMessage));
    }

    public void sendOptionEvent(SendOptionEvent sendOptionEvent) {
        optionsLayout.setVisibility(View.GONE);
        optionsLayout.removeAllViews();
        ChatMessage chatMessage = new ChatMessage(username, sendOptionEvent.getOption().getLabel(), name, true);
        addMessage(chatMessage);
        chatMessage.setMessageValue(sendOptionEvent.getOption().getValue());
        EventBus.getDefault().post(new SendMessageEvent(chatMessage));
    }

    @Subscribe
    public void onEvent(ChatUpdatedEvent event) {
        if (chatMessages.size() > 0) {
            ChatMessage chatMessage = chatMessages.get(chatMessages.size() - 1);
            if (!chatMessage.isYou() && chatMessage.getChatType() == ChatType.QUESTION) {
                Question question = chatMessage.getChatResponse().getQuestion();
                //editText.setHint(question.getLabel());
                //editText.setInputType(FieldType.getInputType(question.getFieldType()));

                if (question.getOptions() != null && question.getOptions().size() > 0) {
                    addOptions(question);
                }
            } else {
                optionsLayout.removeAllViews();
                initiateSendMessageListener();
            }
        }
    }

    @Subscribe
    public void onEvent(MessageAcknowledgementEvent event) {
        ChatMessage chatMessage = event.getChatMessage();
        if (chatMessage.getUsername() != null && chatMessage.getUsername().equals(username)) {
            for (ChatMessage message : chatMessages) {
                if ((message.getStanzaId() != null && message.getStanzaId().equals(chatMessage.getStanzaId())) || (message.getId() != null && message.getId().equals(chatMessage.getId()))) {
                    message.setAcknowledged(chatMessage.getAcknowledged());
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void initiateSendMessageListener() {
        //editText.setHint(getResources().getString(R.string.chat_hint));
        //editText.setInputType(InputType.TYPE_CLASS_TEXT);
    }


    public void addOptions(final Question question) {
        // Remove all the options and scroll left
        optionsLayout.removeAllViews();
        ((HorizontalScrollView) optionsLayout.getParent()).scrollTo(0, optionsLayout.getScrollY());

        List<Option> options = question.getFilteredOptions() != null ? question.getFilteredOptions() : question.getOptions();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin5, margin5, margin5, margin5);

        if (options != null) {
            for (final Option option : options) {
                // Add Option button
                createOptionButton(option.getLabel(), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendOption(question, option);
                    }
                }).setTag(question);
            }
        }

        if (options != null && options.size() > 0) {
            optionsLayout.setVisibility(View.VISIBLE);
        } else {
            optionsLayout.setVisibility(View.GONE);
        }
    }

    public void sendOption(final Question question, final Option option) {
        if (option.isLocation()) {
            askForLocation();
        } else {
            optionsLayout.setVisibility(View.GONE);
            optionsLayout.removeAllViews();
            ChatMessage chatMessage = new ChatMessage(username, option.getLabel(), name, true);
            chatMessage.setMessageValue(option.getValue());
            EventBus.getDefault().post(new SendMessageEvent(chatMessage));
            addMessage(chatMessage);
        }
    }


    GoogleApiClient mGoogleApiClient = null;
    private void askForLocation(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    //
    public TextView createOptionButton(String text, View.OnClickListener onClickListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin5, 0, margin5, margin5);
        TextView optionButton = new TextView(this);
        optionButton.setPadding(padding20, padding10, padding20, padding10);
        optionButton.setGravity(Gravity.CENTER);
        optionButton.setText(Html.fromHtml(text));
        optionButton.setTextSize(size15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            optionButton.setElevation(elevation);
        }
        optionButton.setBackgroundResource(R.drawable.option_button_background);
        optionButton.setLayoutParams(layoutParams);
        optionButton.setOnClickListener(onClickListener);
        optionButton.setTextColor(Color.WHITE);
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        optionButton.setMaxWidth(width);
        optionsLayout.addView(optionButton);
        return optionButton;
    }

    ProductFragment productFragment = new ProductFragment();
    boolean isProductOpen = false;

    private void hideKeyboard() {
        // Change focus from the text box
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openProductView(Product product) {
        hideKeyboard();
        //removeProductView();
        productFragment.setProduct(product);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isProductOpen) {
            fragmentTransaction.detach(productFragment);
            fragmentTransaction.attach(productFragment);
        } else {
            fragmentTransaction.add(R.id.overlay_fragment_container, productFragment);
        }
        fragmentTransaction.commit();
        isProductOpen = true;
        findViewById(R.id.overlay_fragment_container).setVisibility(View.VISIBLE);
    }


    private void removeProductView() {
        if (isProductOpen) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if (productFragment != null) {
                fragmentTransaction.remove(productFragment);
            }
            fragmentTransaction.commit();
            isProductOpen = false;
            findViewById(R.id.overlay_fragment_container).setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isProductOpen) {
                removeProductView();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    // Image Upload

    static int REQUEST_CAMERA = 100;
    static int SELECT_FILE = 101;
    static int PLACE_PICKER_REQUEST = 102;
    Uri fileUri = null;
    boolean fromUpload  = false;

    public void selectImage(View view) {
        final CharSequence[] items = { getString(R.string.take_picture), getString(R.string.choose_photos) , getString(R.string.share_location)};
        AlertDialog.Builder builder = new AlertDialog.Builder(RecyclerChatActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item){
                    case 0:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                        startActivityForResult(intent, REQUEST_CAMERA);
                        break;
                    case 1:
                        Intent intent2 = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent2.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(intent2, "Select File"),
                                SELECT_FILE);
                        break;
                    case 2:
                        askForLocation();
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "YellowMessenger");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("YellowMessenger", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && fileUri!=null) {
                fromUpload = true;
                String filename = "upload_"+(new Date()).getTime()+".jpeg";

                String selectedImagePath = fileUri.getPath();

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);

                Matrix m = new Matrix();
                m.postRotate(S3Utils.getImageOrientation(selectedImagePath));
                Bitmap newBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);


                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                newBmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);

                File destination = new File(selectedImagePath+".2");
                FileOutputStream fo;
                try {
                    boolean created = destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bos.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sendImageMessage(newBmp, filename);

                fetchUploadUrl(destination.getAbsolutePath(),filename);
            } else if (requestCode == SELECT_FILE) {
                fromUpload = true;

                String filename = "upload_"+(new Date()).getTime()+".jpeg";
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bm = BitmapFactory.decodeFile(selectedImagePath, options);

                sendImageMessage(bm, filename);

                fetchUploadUrl(selectedImagePath,filename);
            }
            else
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(this,data);
                if(place !=null){
                    sendLocation(place.getLatLng(),place.getName().toString(),place.getAddress().toString());
                }

            }
        }
    }

    RequestQueue queue = null;
    public void fetchUploadUrl(final String filePath, final String filename){
        try{
            String url = "https://yellowmessenger.com/api/getPolicyParams?username="+ PreferencesManager.getInstance(getApplicationContext()).getXMPPUser().getUsername();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        String policyEncoded  = response.getString("policyEncoded");
                        String signature  = response.getString("signature");
                        String accessKey  = response.getString("accessKey");
                        S3Utils.uploadMultipart(getApplicationContext(), filePath, PreferencesManager.getInstance(getApplicationContext()).getXMPPUser(), policyEncoded, signature, accessKey, filename);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error1) {
                    error1.printStackTrace();
                }
            });
            queue.add(jsonObjectRequest);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendLocation(LatLng latLng, String locationName, String address){
        optionsLayout.setVisibility(View.GONE);
        optionsLayout.removeAllViews();

        try{
            JSONObject locationObj = new JSONObject();
            locationObj.put("lat",latLng.latitude);
            locationObj.put("lng",latLng.longitude);
            locationObj.put("name",locationName);
            locationObj.put("address",address);
            JSONObject messageObject = new JSONObject();
            messageObject.put("location",locationObj);

            final ChatMessage chatMessage = new ChatMessage(username,messageObject.toString(),name,true);
            addMessage(chatMessage);
            EventBus.getDefault().post(new SendMessageEvent(chatMessage));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void sendImageMessage(Bitmap bm, String filename) {
        String message = "{\"type\":\"image\",\"image\":\"https://s3-ap-southeast-1.amazonaws.com/consoleuploads/uploads/"+ PreferencesManager.getInstance(getApplicationContext()).getXMPPUser().getUsername()+ "/" + filename+"\"}";
        ChatMessage chatMessage = new ChatMessage(username,message,name,true);
        chatMessage.setBitmap(bm);
        addMessage(chatMessage);
        EventBus.getDefault().post(new UploadStartEvent(chatMessage, filename));
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

