package com.yellowmessenger.sdk.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.yellowmessenger.sdk.ResultsActivity;
import com.yellowmessenger.sdk.models.ChatResponse;
import com.yellowmessenger.sdk.models.Product;
import com.yellowmessenger.sdk.utils.DrawableManager;
import com.yellowmessenger.sdk.utils.ImageViewerAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kishore on 23/05/16.
 */
public class ProductFragment extends Fragment {

    Gson gson;
    Product product;
    RequestQueue queue;
    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        gson = new Gson();
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    ImageViewerAdapter imageViewerAdapter;
    List<String> images;
    ViewPager viewPager;
    LinearLayout mPagerIndicator;
    int indicatorSelectedColor;
    int indicatorColor;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        indicatorColor = Color.parseColor("#cccccc");
        indicatorSelectedColor = Color.parseColor("#444444");
        queue = Volley.newRequestQueue(getActivity());
        viewPager = (ViewPager) view.findViewById(R.id.imageViewer);
        images = new ArrayList<>();
        imageViewerAdapter = new ImageViewerAdapter(getActivity(),images);
        viewPager.setAdapter(imageViewerAdapter);
        mPagerIndicator = (LinearLayout) view.findViewById(R.id.indicator);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i = 0;i<mPagerIndicator.getChildCount();i++){
                    mPagerIndicator.getChildAt(i).setBackgroundColor(indicatorColor);
                }
                mPagerIndicator.getChildAt(position).setBackgroundColor(indicatorSelectedColor);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        showProduct();
        loadProduct();
    }

    private void initiateIndicators(){
        mPagerIndicator.removeAllViews();
        int indicatorWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        int indicatorSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(indicatorWidth,indicatorHeight);
        layoutParams.setMargins(indicatorSpacing,0,indicatorSpacing,0);
        for(int i = 0 ; i < images.size();i++){
            View view = getActivity().getLayoutInflater().inflate(R.layout.indicator_view, null);
            if(i==0){
                view.setBackgroundColor(indicatorSelectedColor);
            }else{
                view.setBackgroundColor(indicatorColor);
            }
            view.setLayoutParams(layoutParams);
            mPagerIndicator.addView(view);
        }
    }

    public void showProduct(){
        if(getView()!=null && getActivity()!=null && product!=null){
            getActivity().runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    if(getActivity()!=null){
                        images.clear();
                        if(product.getImageUrls()!=null && product.getImageUrls().size()>0){
                            images.addAll(product.getImageUrls());
                        }else{
                            images.add(product.getImage());
                        }
                        imageViewerAdapter.notifyDataSetChanged();
                        initiateIndicators();
                    }
                }
            });


            View buyButton = getView().findViewById(R.id.buy_layout);

            TextView productName = (TextView) getView().findViewById(R.id.product_name);
            TextView productPrice = (TextView) getView().findViewById(R.id.product_price);
            TextView productPriceOriginal = (TextView) getView().findViewById(R.id.product_price_original);
            TextView productDiscount = (TextView) getView().findViewById(R.id.product_discount);


            productName.setText(product.getName());
            productPrice.setText(Html.fromHtml(product.getPrice()));


            if (product.getPriceOriginal() != null && !product.getPriceOriginal().equals(product.getPrice())) {
                productPriceOriginal.setText(Html.fromHtml(product.getPriceOriginal()));
                productPriceOriginal.setPaintFlags(productPriceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                productPriceOriginal.setVisibility(View.VISIBLE);

                if (product.getDiscount() != null) {
                    productDiscount.setText(Html.fromHtml(product.getDiscount()));
                    productDiscount.setVisibility(View.VISIBLE);
                }
            } else {
                productPriceOriginal.setVisibility(View.INVISIBLE);
                productDiscount.setVisibility(View.INVISIBLE);
            }

            if(product.getDescription()!=null){
                TextView description = (TextView) getView().findViewById(R.id.description);
                description.setText(Html.fromHtml(product.getDescription()));
            }

            buyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(product.getUrl()));
                    startActivity(i);
                }
            });

            if(product.getSimilarProducts()!=null && product.getSimilarProducts().size()>0){
                getView().findViewById(R.id.similar_products_label).setVisibility(View.VISIBLE);
                int imageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
                int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getResources().getDisplayMetrics());
                int priceSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 13, getResources().getDisplayMetrics());
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                int pricePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                int paddingProduct = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

                for(final Product similarProduct:product.getSimilarProducts()){
                    LinearLayout.LayoutParams linearLayoutParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayoutParams.setMargins(0,0,padding,0);
                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    linearLayout.setPadding(paddingProduct,paddingProduct,paddingProduct,paddingProduct);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(linearLayoutParams);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        linearLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.product_background, null));
                    }else{
                        linearLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.product_background, null));
                    }

                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(getActivity() instanceof ChatActivity){
                                ((ChatActivity)getActivity()).openProductView(similarProduct);
                            }else
                            if(getActivity() instanceof ResultsActivity){
                                // ((ResultsActivity)getActivity()).openProductView(similarProduct);
                            }
                        }
                    });

                    LinearLayout.LayoutParams imageLayoutParams= new LinearLayout.LayoutParams(imageWidth, imageHeight);
                    ImageView similarImageView = new ImageView(getActivity());
                    similarImageView.setAdjustViewBounds(true);
                    similarImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    similarImageView.setLayoutParams(imageLayoutParams);


                    linearLayout.addView(similarImageView);
                    DrawableManager.getInstance(getActivity()).fetchDrawableOnThread(similarProduct.getImage(),similarImageView);

                    LinearLayout.LayoutParams priceLayoutParams= new LinearLayout.LayoutParams(imageWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout priceLayout = new LinearLayout(getActivity());
                    priceLayout.setOrientation(LinearLayout.HORIZONTAL);
                    priceLayout.setLayoutParams(priceLayoutParams);
                    priceLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.white,null));

                    LinearLayout.LayoutParams priceParams= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView price = new TextView(getActivity());
                    price.setLayoutParams(priceParams);
                    price.setText(Html.fromHtml(similarProduct.getPrice()));
                    price.setTypeface(Typeface.SERIF,Typeface.BOLD);
                    price.setTextColor(ResourcesCompat.getColor(getResources(),R.color.primary_color,null));
                    price.setTextSize(priceSize);
                    price.setPadding(pricePadding,pricePadding,pricePadding,pricePadding);

                    priceLayout.addView(price);


                    if (similarProduct.getPriceOriginal() != null && !similarProduct.getPriceOriginal().equals(similarProduct.getPrice())) {
                        TextView priceOriginal = new TextView(getActivity());
                        priceOriginal.setLayoutParams(priceParams);
                        priceOriginal.setText(Html.fromHtml(similarProduct.getPriceOriginal()));
                        priceOriginal.setTypeface(Typeface.SERIF);
                        priceOriginal.setTextSize(priceSize);
                        priceOriginal.setPaintFlags(priceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        priceOriginal.setPadding(pricePadding,pricePadding,pricePadding,pricePadding);

                        priceLayout.addView(priceOriginal);
                    }


                    linearLayout.addView(priceLayout);
                    ((LinearLayout)getView().findViewById(R.id.similar_products)).addView(linearLayout);
                }
            }
        }else{
            if(getActivity()!=null)getActivity().finish();
        }
    }

    public void loadProduct(){
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, product.getJsonUrl(),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String responseString = response.toString();
                        ChatResponse chatResponse = gson.fromJson(responseString, ChatResponse.class);
                        if(chatResponse.getProduct()!=null){
                            ProductFragment.this.setProduct(chatResponse.getProduct());
                            showProduct();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        stringRequest.setRetryPolicy(retryPolicy);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}
