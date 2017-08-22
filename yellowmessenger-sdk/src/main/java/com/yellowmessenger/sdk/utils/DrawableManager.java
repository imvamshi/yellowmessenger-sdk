package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kishore on 14/07/16.
 */
public class DrawableManager {

    private static DrawableManager drawableManager;
    public static DrawableManager getInstance(Context context){
        if(drawableManager == null){
            DisplayImageOptions defaultOptions =
                    new DisplayImageOptions.Builder()
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .displayer(new FadeInBitmapDisplayer(500))
                            .resetViewBeforeLoading(true)
                            .build();

            File cacheDir = StorageUtils.getOwnCacheDirectory(context, "image_cache");//for caching

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(defaultOptions)
                    .diskCache(new UnlimitedDiskCache(cacheDir))
                    .threadPoolSize(5)
                    .memoryCacheSize(20000000)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                    .build();


            ImageLoader.getInstance().init(config);
            drawableManager = new DrawableManager();
        }

        return drawableManager;
    }


    public void fetchDrawableOnThread(String urlString, ImageView imageView) {
        ImageLoader.getInstance().displayImage(urlString, imageView);
    }

    public Drawable getDrawableFromUrl(String url) throws java.io.IOException {
        return new BitmapDrawable(ImageLoader.getInstance().loadImageSync(url));
    }

}