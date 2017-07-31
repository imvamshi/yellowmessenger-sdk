package com.yellowmessenger.sdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;

public class ResultsActivity extends AppCompatActivity{
    private ListView listViewLeft;
    private ListView listViewRight;

    int[] leftViewsHeights;
    int[] rightViewsHeights;

    RequestQueue queue;

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
