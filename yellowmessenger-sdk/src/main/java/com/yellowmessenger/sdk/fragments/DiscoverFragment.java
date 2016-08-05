package com.yellowmessenger.sdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
import com.yellowmessenger.sdk.models.Featured;
import com.yellowmessenger.sdk.utils.FeaturedAdapter;
import com.yellowmessenger.sdk.utils.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DiscoverFragment extends Fragment {
    Gson gson;
    RequestQueue queue;

    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
            10000,
            10,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        gson = new Gson();
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        featuredAdapter = new FeaturedAdapter(getActivity().getApplicationContext(),featuredObjects);
        try{
            loadFeatured();
            fetchFeaturedBots();
        }catch (Exception e){

        }

    }

    List<Featured> featuredObjects = new ArrayList<>();

    public void fetchFeaturedBots() throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("country", PreferencesManager.getInstance(getContext()).getCountry());
            jsonObject.put("type", PreferencesManager.getInstance(getContext()).getAccount());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = "https://corona.yellowmessenger.com/settings";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
        new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success") && getActivity() != null) {
                        JSONArray objects = response.getJSONArray("data");
                        if (objects != null && objects.length() > 0) {
                            final List<Featured> featuredList = new LinkedList<>();
                            JSONArray featuredObjs = objects.getJSONObject(0).getJSONArray("value");
                            for (int i = 0; i < featuredObjs.length(); i++) {
                                JSONObject featuredObj = featuredObjs.getJSONObject(i);
                                featuredList.add(gson.fromJson(featuredObj.toString(),Featured.class));
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DiscoverFragment.this.featuredObjects.clear();
                                    DiscoverFragment.this.featuredObjects.addAll(featuredList);
                                    DiscoverFragment.this.featuredAdapter.notifyDataSetChanged();
                                }
                            });
                        }
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
        queue.add(jsonObjectRequest);
    }

    GridView featuredGrid;
    FeaturedAdapter featuredAdapter;

    public void loadFeatured() {
        if (getView() != null && getActivity() != null && featuredObjects!=null) {
            featuredGrid = (GridView) getView().findViewById(R.id.featured_grid);
            featuredGrid.setAdapter(featuredAdapter);
            featuredGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Featured featured = featuredObjects.get(position);
                    if(featured.getUsername() != null){
                        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Bundle bundle = new Bundle();

                        bundle.putString("username", featured.getUsername());
                        bundle.putString("name", featured.getName());

                        intent.putExtras(bundle);
                        startActivityForResult(intent, 0);
                        // TODO Analytics.getInstance(getActivity().getApplicationContext()).track("featured","business", featured.getUsername());
                    }
                }
            });
        }
    }


}
