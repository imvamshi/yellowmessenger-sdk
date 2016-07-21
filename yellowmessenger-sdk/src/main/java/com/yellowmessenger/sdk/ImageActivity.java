package com.yellowmessenger.sdk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.yellowmessenger.sdk.fragments.ImageFragment;

import java.util.ArrayList;

public class ImageActivity extends FragmentActivity {
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    ArrayList<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);

        urls  = getIntent().getExtras().getStringArrayList("urls");
        int position = getIntent().getExtras().getInt("position");

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),urls.size());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position,true);

    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        int count;
        public ScreenSlidePagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.count = count;
        }

        @Override
        public Fragment getItem(int position) {
            ImageFragment fragment = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url",urls.get(position));
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return this.count;
        }
    }
}
