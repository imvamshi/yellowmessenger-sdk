package com.yellowmessenger.sdk;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
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

import com.yellowmessenger.sdk.dao.ChatMessageDAO;
import com.yellowmessenger.sdk.events.ChatConnectedEvent;
import com.yellowmessenger.sdk.events.ChatDisconnectedEvent;
import com.yellowmessenger.sdk.events.ChatUpdatedEvent;
import com.yellowmessenger.sdk.events.MessageAcknowledgementEvent;
import com.yellowmessenger.sdk.events.MessageReceivedEvent;
import com.yellowmessenger.sdk.events.SendActionEvent;
import com.yellowmessenger.sdk.events.SendMessageEvent;
import com.yellowmessenger.sdk.fragments.ProductFragment;
import com.yellowmessenger.sdk.models.ChatType;
import com.yellowmessenger.sdk.models.FieldType;
import com.yellowmessenger.sdk.models.Option;
import com.yellowmessenger.sdk.models.Product;
import com.yellowmessenger.sdk.models.Question;
import com.yellowmessenger.sdk.models.db.ChatMessage;
import com.yellowmessenger.sdk.utils.ChatListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChatActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_chat);
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
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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

    // TODO items


    public void sendActionEvent(SendActionEvent sendActionEvent) {

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
                initiateSendMessageListener();
            }
        }
    }

    @Subscribe
    public void onEvent(MessageAcknowledgementEvent event) {
        ChatMessage chatMessage = event.getChatMessage();
        if(chatMessage.getUsername()!=null && chatMessage.getUsername().equals(username)){
            for(ChatMessage message:chatMessages){
                if((message.getStanzaId()!=null && message.getStanzaId().equals(chatMessage.getStanzaId())) || (message.getId()!=null && message.getId().equals(chatMessage.getId()))){
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


    private void askForLocation() {

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
    private void hideKeyboard(){
        // Change focus from the text box
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openProductView(Product product) {
        hideKeyboard();
        //removeProductView();
        productFragment.setProduct(product);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(isProductOpen){
            fragmentTransaction.detach(productFragment);
            fragmentTransaction.attach(productFragment);
        }else{
            fragmentTransaction.add(R.id.overlay_fragment_container,productFragment);
        }
        fragmentTransaction.commit();
        isProductOpen = true;
        findViewById(R.id.overlay_fragment_container).setVisibility(View.VISIBLE);
    }


    private void removeProductView() {
        if (isProductOpen) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            if(productFragment!=null){
                fragmentTransaction.remove(productFragment);
            }
            fragmentTransaction.commit();
            isProductOpen = false;
            findViewById(R.id.overlay_fragment_container).setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if(isProductOpen){
                removeProductView();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
