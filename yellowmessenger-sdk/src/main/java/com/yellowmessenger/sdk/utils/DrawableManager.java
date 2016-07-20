package com.yellowmessenger.sdk.utils;

import android.content.Context;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by kishore on 14/07/16.
 */
public class DrawableManager {
    private Context context;

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
            drawableManager = new DrawableManager(context);
        }

        return drawableManager;
    }

    private  DrawableManager(Context context) {
        this.context = context;
    }

    public void fetchDrawableOnThread(String urlString, ImageView imageView) {
        ImageLoader.getInstance().displayImage(urlString, imageView);
    }

}