package com.yellowmessenger.sdk;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;

public class PDFActivity extends AppCompatActivity implements DownloadFile.Listener {
    RemotePDFViewPager remotePDFViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        remotePDFViewPager = new RemotePDFViewPager(this.getApplicationContext(), getIntent().getExtras().getString("url"), this);
    }

    @Override
    public void onSuccess(String url, String destinationPath) {
        PDFPagerAdapter adapter = new PDFPagerAdapter(this.getApplicationContext(), destinationPath);
        remotePDFViewPager.setAdapter(adapter);
        String[] arr = getIntent().getExtras().getString("url").split("#");
        remotePDFViewPager.setCurrentItem(arr.length > 1 ? Integer.parseInt(arr[1].split("=")[1]): 0, true);
        setContentView(remotePDFViewPager);
    }

    @Override
    public void onFailure(Exception e) {

    }

    @Override
    public void onProgressUpdate(int progress, int total) {

    }
}
