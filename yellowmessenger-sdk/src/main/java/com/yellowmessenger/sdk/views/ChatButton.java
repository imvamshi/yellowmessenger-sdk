package com.yellowmessenger.sdk.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.yellowmessenger.sdk.ChatActivity;
import com.yellowmessenger.sdk.utils.PreferencesManager;

public class ChatButton extends FloatingActionButton {
    public ChatButton(Context context) {
        super(context);
        init();
    }

    public ChatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle bundle = new Bundle();

                bundle.putString("username", PreferencesManager.getInstance(getContext()).getAccount());
                bundle.putString("name", PreferencesManager.getInstance(getContext()).getName()!=null?PreferencesManager.getInstance(getContext()).getName():"Chat with us");
                //bundle.putString("username", "csia");
                //bundle.putString("name", "GVK Airport");
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });
    }
}
