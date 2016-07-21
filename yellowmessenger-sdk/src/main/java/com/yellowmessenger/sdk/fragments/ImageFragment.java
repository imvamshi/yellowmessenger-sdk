package com.yellowmessenger.sdk.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yellowmessenger.sdk.R;
import com.yellowmessenger.sdk.utils.DrawableManager;


public class ImageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String url = getArguments().getString("url");
        ImageView imageView = (ImageView) getView().findViewById(R.id.image);
        DrawableManager.getInstance(getActivity().getApplicationContext()).fetchDrawableOnThread(url, imageView);
    }
}