package com.yellowmessenger.sdk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yellowmessenger.sdk.ChatActivity;
import com.yellowmessenger.sdk.ImageActivity;
import com.yellowmessenger.sdk.R;
import com.yellowmessenger.sdk.events.ChatUpdatedEvent;
import com.yellowmessenger.sdk.events.SendActionEvent;
import com.yellowmessenger.sdk.events.SendOptionEvent;
import com.yellowmessenger.sdk.models.Action;
import com.yellowmessenger.sdk.models.ChatResponse;
import com.yellowmessenger.sdk.models.MessageObject;
import com.yellowmessenger.sdk.models.Option;
import com.yellowmessenger.sdk.models.Product;
import com.yellowmessenger.sdk.models.Question;
import com.yellowmessenger.sdk.models.db.ChatMessage;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;

@SuppressWarnings("ALL")
public class ChatListAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;
    private static LayoutInflater inflater;
    private List<ChatMessage> values;
    private String name;
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm dd MMM, yyyy", Locale.getDefault());
    Gson gson = new Gson();
    int margin42;

    private static class MessageViewHolder{
        TextView message;
        TextView timestamp;
    }

    private static class QuestionViewHolder{
        TextView message;
        TextView timestamp;
        View actionButton;
        TextView actionText;

        List<View> persistentOptions = new ArrayList<>();
    }

    private static class DeepLinkViewHolder{
        TextView message;
        TextView timestamp;
        View deepLinkButton;
        TextView buyButton;
    }

    private static class LocationViewHolder{
        TextView timestamp;
        ImageView mapView;
    }

    private static class ProductViewHolder{
        TextView productName;
        TextView productPrice;
        TextView productPriceOriginal;
        TextView productPriceDiscount;
        TextView timestamp;
        ImageView imageView;
        TextView productDescription;
        TextView buyButton;

        View buy;
        View productActions;
        View productImageLayout;
        View productDetails;
    }

    private static class ViewHolderOwn{
        TextView message;
        TextView timestamp;
        View messageHolder;

        TextView productTitle;
        TextView productPrice;
        ImageView productImage;
        View productLayout;
    }

    private static class SearchViewHolder{
        TextView message;
        TextView timestamp;
        List<View> productsLayouts = new ArrayList<>();

        HorizontalScrollView scrollView;

    }

    public ChatListAdapter(Context context, List<ChatMessage> values, String name) {
        super(context, R.layout.chat_list_item, values);
        inflater = LayoutInflater.from( context);
        this.context = context;
        this.values = values;
        this.name = name;
        this.margin42 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, context.getResources().getDisplayMetrics());
        EventBus.getDefault().post(new ChatUpdatedEvent(true));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)){
            case 0:
                return getOwnView(position, convertView, parent);
            case 1:
                return getMessageView(position, convertView, parent);
            case 2:
                return getProductView(position, convertView, parent);
            case 3:
                return getSearchView(position, convertView, parent,false);
            case 4:
                return getQuestionView(position, convertView, parent);
            case 5:
                return getDeepLinkView(position, convertView, parent);
            case 6:
                return getLocationView(position, convertView, parent);
            case 7:
                return getSearchView(position, convertView, parent,false);
            default:
                return getMessageView(position,convertView,parent);
        }
    }

    private View getLocationView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        LocationViewHolder locationViewHolder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_list_item_location, parent, false);
            locationViewHolder = new LocationViewHolder();
            locationViewHolder.mapView = (ImageView) view.findViewById(R.id.map_view);
            locationViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            view.setTag(locationViewHolder);
        } else {
            locationViewHolder = (LocationViewHolder) view.getTag();
        }
        try {
            String markers = "color:red%7Clabel:A%7C"+ chatMessage.getChatResponse().getLocation().getLat() + "," + chatMessage.getChatResponse().getLocation().getLng();
            String url = "http://maps.google.com/maps/api/staticmap?center=" + chatMessage.getChatResponse().getLocation().getLat() + "," + chatMessage.getChatResponse().getLocation().getLng()+"&scale=2&markers="+ markers+ "&zoom=15&size=280x140&sensor=false";
            DrawableManager.getInstance(getContext()).fetchDrawableOnThread(url,locationViewHolder.mapView);
            String timestamp = DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString()+" - "+(chatMessage.getAcknowledged()?"Sent":"Sending..");
            locationViewHolder.timestamp.setText(timestamp);
            locationViewHolder.mapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                            chatMessage.getChatResponse().getLocation().getLat(),
                            chatMessage.getChatResponse().getLocation().getLng(),
                            chatMessage.getChatResponse().getLocation().getLat(),
                            chatMessage.getChatResponse().getLocation().getLng(),
                            chatMessage.getChatResponse().getLocation().getName()!=null?chatMessage.getChatResponse().getLocation().getName():"");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private View getDeepLinkView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        DeepLinkViewHolder deepLinkViewHolder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_list_item_deeplink, parent, false);
            deepLinkViewHolder = new DeepLinkViewHolder();
            deepLinkViewHolder.message = (TextView) view.findViewById(R.id.message);
            deepLinkViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            deepLinkViewHolder.deepLinkButton = view.findViewById(R.id.deep_link_button);
            deepLinkViewHolder.buyButton = (TextView) view.findViewById(R.id.buy_button);
            view.setTag(deepLinkViewHolder);
        } else {
            deepLinkViewHolder = (DeepLinkViewHolder) view.getTag();
        }
        deepLinkViewHolder.message.setText(chatMessage.getChatResponse().getDeepLink().getMessage());
        deepLinkViewHolder.buyButton.setText(chatMessage.getChatResponse().getDeepLink().getButtonText().toUpperCase());

        deepLinkViewHolder.deepLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle bundle = new Bundle();
                bundle.putString("user", chatMessage.getChatResponse().getDeepLink().getUser());
                bundle.putString("placeId", chatMessage.getChatResponse().getDeepLink().getPlaceId());
                bundle.putString("name", chatMessage.getChatResponse().getDeepLink().getName());
                if(chatMessage.getChatResponse().getDeepLink().getAction() != null){
                    bundle.putString("action", chatMessage.getChatResponse().getDeepLink().getAction());
                }
                if(chatMessage.getChatResponse().getDeepLink().getCategory() != null){
                    bundle.putString("category", chatMessage.getChatResponse().getDeepLink().getCategory());
                }
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        try {
            deepLinkViewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return view;
    }

    private View getQuestionView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        QuestionViewHolder questionViewHolder = null;
        if (view == null) {
                view = inflater.inflate(R.layout.chat_list_item_question, parent, false);
            questionViewHolder = new QuestionViewHolder();
            questionViewHolder.message = (TextView) view.findViewById(R.id.message);
            questionViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            questionViewHolder.actionButton = view.findViewById(R.id.action_button);
            questionViewHolder.actionText = (TextView)view.findViewById(R.id.action_text);

            for(int i = 0; i < 10; i++){
                View optionView = inflater.inflate(R.layout.options_view, parent, false);
                ((ViewGroup)view.findViewById(R.id.persistentOptions)).addView(optionView);
                questionViewHolder.persistentOptions.add(optionView);
            }

            view.setTag(questionViewHolder);
        } else {
            questionViewHolder = (QuestionViewHolder) view.getTag();
        }
        final Question question =  chatMessage.getChatResponse().getQuestion();
        questionViewHolder.message.setText(chatMessage.getChatResponse().getQuestion().getQuestion());

        if(question.getAction()!=null)
        {
            questionViewHolder.actionButton.setVisibility(View.VISIBLE);
            questionViewHolder.actionText.setText(question.getAction().getLabel());
            questionViewHolder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ChatActivity)context).sendOptionEvent(new SendOptionEvent(question.getAction()));
                }
            });

        }else{
            questionViewHolder.actionButton.setVisibility(GONE);
        }

        for(View optionView : questionViewHolder.persistentOptions){
            optionView.setVisibility(GONE);
            optionView.findViewById(R.id.button_1).setVisibility(GONE);
            optionView.findViewById(R.id.button_2).setVisibility(GONE);
            optionView.findViewById(R.id.button_3).setVisibility(GONE);
        }

        if(question.isPersistentOptions() && question.getOptions()!=null && question.getOptions().size()>0){
            for(int i = 0 ; i < question.getOptions().size();i++){
                View optionView = questionViewHolder.persistentOptions.get(i / 3);
                optionView.setVisibility(View.VISIBLE);
                int buttonNumber = i % 3;
                TextView button = null;
                if(buttonNumber == 0 ){
                    button = (TextView) optionView.findViewById(R.id.button_1);
                }else
                if(buttonNumber == 1){
                    button = (TextView) optionView.findViewById(R.id.button_2);
                }else{
                    button = (TextView) optionView.findViewById(R.id.button_3);
                }
                final Option option = question.getOptions().get(i);
                button.setVisibility(View.VISIBLE);
                button.setText(question.getOptions().get(i).getLabel());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChatActivity)context).sendOptionEvent(new SendOptionEvent(option));
                    }
                });
            }
        }

        try {
            questionViewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return view;
    }

    private View getSearchView(int position, View convertView, ViewGroup parent,boolean portraitMode) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        SearchViewHolder searchViewHolder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_list_item_search, parent, false);
            searchViewHolder = new SearchViewHolder();
            searchViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            searchViewHolder.message = (TextView) view.findViewById(R.id.message);
            for(int i = 0; i < 10; i++){
                View productView = inflater.inflate(portraitMode?R.layout.list_product_portrait_view:R.layout.list_product_view, parent, false);
                ((ViewGroup)view.findViewById(R.id.productsView)).addView(productView);
                searchViewHolder.productsLayouts.add(productView);
            }

            searchViewHolder.scrollView = (HorizontalScrollView) view.findViewById(R.id.scrollView);

            view.setTag(searchViewHolder);
        }
        else {
            searchViewHolder = (SearchViewHolder) view.getTag();
        }

        searchViewHolder.scrollView.scrollTo(0,0);
        final ChatResponse chatResponse = chatMessage.getChatResponse();
        if(chatResponse.getSearchResults().getMessage()!=null){
            searchViewHolder.message.setVisibility(View.VISIBLE);
            searchViewHolder.message.setText(chatResponse.getSearchResults().getMessage());
        }else{
            searchViewHolder.message.setVisibility(GONE);
        }

        int productSize = chatResponse.getSearchResults().getProducts()!=null?chatResponse.getSearchResults().getProducts().size():0;
        final List<Product> products = chatResponse.getSearchResults().getProducts();
        if(productSize == 0){
            for(int i =0; i < 10; i++){
                searchViewHolder.productsLayouts.get(i).setVisibility(GONE);
            }
        }

        for(int i = 0; i < 10; i++){
            final int index = i;
            if(productSize>i){
                searchViewHolder.productsLayouts.get(i).setVisibility(View.VISIBLE);

                searchViewHolder.productsLayouts.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(products.get(index).getJsonUrl()!=null){
                            ((ChatActivity)context).openProductView(products.get(index));
                        }
                    }
                });

                ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_name)).setText(products.get(i).getName());
                if(products.get(i).getPrice()!=null){
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price).setVisibility(View.VISIBLE);
                    ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price)).setText(Html.fromHtml(products.get(i).getPrice()));
                }else{
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price).setVisibility(GONE);
                }

                if (products.get(i).getPriceOriginal() != null && !products.get(i).getPriceOriginal().equals(products.get(i).getPrice())) {
                    ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price_original)).setText(Html.fromHtml(products.get(i).getPriceOriginal()));
                    ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price_original)).setPaintFlags(((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price_original)).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price_original).setVisibility(View.VISIBLE);

                    if (products.get(i).getDiscount() != null) {
                        ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_discount)).setText(Html.fromHtml(products.get(i).getDiscount()));
                        searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_discount).setVisibility(View.VISIBLE);
                    }
                } else {
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_price_original).setVisibility(GONE);
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_discount).setVisibility(GONE);
                }
                if(products.get(i).getDescription()!=null){
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_description).setVisibility(View.VISIBLE);
                    ((TextView)searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_description)).setText(Html.fromHtml(products.get(i).getDescription()));
                }else{
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_description).setVisibility(GONE);
                }

                TextView buttonPrimary = (TextView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_action_button);
                TextView buttonSecondary = (TextView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_action_button_secondary);

                TextView button1 = (TextView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_action_button_1);
                TextView button2 = (TextView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_action_button_2);
                TextView button3 = (TextView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_action_button_3);

                searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_details).setVisibility(View.VISIBLE);

                if(products.get(i).getActions()!=null && products.get(i).getActions().size()>0){
                    if(products.get(i).getActions().size()>2 || chatResponse.getSearchResults().isSelection()){
                        button1.setVisibility(View.GONE);
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);


                        for(int x = 0 ; x < products.get(i).getActions().size(); x++){
                            final Action action =  products.get(i).getActions().get(x);
                            switch (x){
                                case 0:
                                    button1.setVisibility(View.VISIBLE);
                                    button1.setText(action.getTitle());
                                    button1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(action.getUrl()!=null){
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse(action.getUrl()));
                                                context.startActivity(i);
                                            }else{
                                                ((ChatActivity)context).sendActionEvent(new SendActionEvent(action));
                                            }
                                        }
                                    });
                                    break;
                                case 1:
                                    button2.setVisibility(View.VISIBLE);
                                    button2.setText(action.getTitle());
                                    button2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(action.getUrl()!=null){
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse(action.getUrl()));
                                                context.startActivity(i);
                                            }else{
                                                ((ChatActivity)context).sendActionEvent(new SendActionEvent(action));
                                            }
                                        }
                                    });
                                    break;
                                case 2:
                                    button3.setVisibility(View.VISIBLE);
                                    button3.setText(action.getTitle());
                                    button3.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(action.getUrl()!=null){
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse(action.getUrl()));
                                                context.startActivity(i);
                                            }else{
                                                ((ChatActivity)context).sendActionEvent(new SendActionEvent(action));
                                            }
                                        }
                                    });
                                    break;
                            }

                            if(x==2){
                                break;
                            }
                        }
                        buttonPrimary.setVisibility(GONE);
                        buttonSecondary.setVisibility(GONE);
                    }
                    else{
                        button1.setVisibility(View.GONE);
                        button2.setVisibility(View.GONE);
                        button3.setVisibility(View.GONE);

                        buttonPrimary.setVisibility(View.VISIBLE);
                        buttonPrimary.setText(products.get(i).getActions().get(0).getTitle());
                        final Action action = products.get(i).getActions().get(0);
                        buttonPrimary.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(action.getUrl()!=null){
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(action.getUrl()));
                                    context.startActivity(i);
                                }else{

                                    ((ChatActivity)context).sendActionEvent(new SendActionEvent(action));
                                }
                            }
                        });

                        if(products.get(i).getActions().size()>1){
                            buttonSecondary.setVisibility(View.VISIBLE);
                            buttonSecondary.setText(products.get(i).getActions().get(1).getTitle());
                            final Action actionSecondary = products.get(i).getActions().get(1);
                            buttonSecondary.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(actionSecondary.getUrl()!=null){
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(actionSecondary.getUrl()));
                                        context.startActivity(i);
                                    }else{
                                        ((ChatActivity)context).sendActionEvent(new SendActionEvent(actionSecondary));
                                    }
                                }
                            });
                        }else {
                            buttonSecondary.setVisibility(GONE);
                        }
                    }
                }else{
                    buttonPrimary.setVisibility(GONE);
                    buttonSecondary.setVisibility(GONE);
                }

                int detailsMarginBottom = products.get(i).getActions()!=null?(products.get(i).getActions().size()>1? margin42 *2:(products.get(i).getActions().size()>0)? margin42 :0):0;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0,0,0,detailsMarginBottom);
                searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_details).setLayoutParams(layoutParams);


                if(products.get(i).getImage() != null){
                    DrawableManager.getInstance(context).fetchDrawableOnThread(products.get(i).getImage(), ((ImageView) searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_image)));
                }else{
                    searchViewHolder.productsLayouts.get(i).findViewById(R.id.product_image).setVisibility(GONE);
                }

            }else{
                searchViewHolder.productsLayouts.get(i).setVisibility(GONE);
            }
        }

        try {
            searchViewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return view;
    }

    private View getProductView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        ProductViewHolder productViewHolder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_list_item_product, parent, false);
            productViewHolder = new ProductViewHolder();
            productViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            productViewHolder.productName = (TextView) view.findViewById(R.id.product_name);
            productViewHolder.productPrice = (TextView) view.findViewById(R.id.product_price);
            productViewHolder.productPriceOriginal = (TextView) view.findViewById(R.id.product_price_original);
            productViewHolder.productImageLayout = view.findViewById(R.id.product_image_layout);
            productViewHolder.productPriceDiscount = (TextView) view.findViewById(R.id.product_price_discount);
            productViewHolder.productDescription = (TextView) view.findViewById(R.id.product_description);
            productViewHolder.buy = view.findViewById(R.id.buy_layout);
            productViewHolder.productActions = view.findViewById(R.id.product_actions);
            productViewHolder.imageView = (ImageView) view.findViewById(R.id.product_image);
            productViewHolder.buyButton =  (TextView) view.findViewById(R.id.buy_button);
            productViewHolder.productDetails = view.findViewById(R.id.product_details);
            view.setTag(productViewHolder);
        } else {
            productViewHolder = (ProductViewHolder) view.getTag();
        }
        productViewHolder.productActions.setVisibility(GONE);
        productViewHolder.timestamp.setVisibility(View.VISIBLE);

        try {
            productViewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }


        ChatResponse chatResponse = chatMessage.getChatResponse();

        if (chatResponse != null) {
            final Product product = chatResponse.getProduct();
            if (product != null) {
                productViewHolder.imageView.setVisibility(GONE);
                if(product.getName()!=null){
                    productViewHolder.productName.setVisibility(View.VISIBLE);
                    productViewHolder.productName.setText(product.getName());
                    productViewHolder.productDetails.setVisibility(View.VISIBLE);
                }else{
                    productViewHolder.productName.setVisibility(GONE);
                    productViewHolder.productDetails.setVisibility(GONE);
                }

                if (product.getPrice() != null) {
                    productViewHolder.productPrice.setText(Html.fromHtml(product.getPrice()));
                    productViewHolder.productPrice.setVisibility(View.VISIBLE);
                } else {
                    productViewHolder.productPrice.setVisibility(GONE);
                }

                if (product.getPriceOriginal() != null && !product.getPriceOriginal().equals(product.getPrice())) {
                    productViewHolder.productPriceOriginal.setText(Html.fromHtml(product.getPriceOriginal()));
                    productViewHolder.productPriceOriginal.setPaintFlags(productViewHolder.productPriceOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    productViewHolder.productPriceOriginal.setVisibility(View.VISIBLE);

                    if (product.getDiscount() != null) {
                        productViewHolder.productPriceDiscount.setText(Html.fromHtml(product.getDiscount()));
                        productViewHolder.productPriceDiscount.setVisibility(View.VISIBLE);
                    }
                } else {
                    productViewHolder.productPriceOriginal.setVisibility(GONE);
                    productViewHolder.productPriceDiscount.setVisibility(GONE);
                }

                if (product.getDescription() != null) {
                    productViewHolder.productDescription.setVisibility(View.VISIBLE);
                    productViewHolder.productDescription.setText(Html.fromHtml(product.getDescription()));
                } else {
                    productViewHolder.productDescription.setVisibility(GONE);
                }

                if (product.getUrl() != null) {
                    productViewHolder.productActions.setVisibility(View.VISIBLE);
                    final String buyUrl = product.getUrl();
                    final String buyTitle = product.getAction();
                    productViewHolder.buyButton.setText(Html.fromHtml(product.getAction()));

                    productViewHolder.buy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                } else {
                    productViewHolder.productActions.setVisibility(GONE);
                }

                if (product.getImage() != null) {
                    productViewHolder.imageView.setVisibility(View.VISIBLE);
                    DrawableManager.getInstance(context).fetchDrawableOnThread(product.getImage(), productViewHolder.imageView);
                    final String imageUrl = product.getImage();
                    productViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*Intent intent = new Intent(context, ImageActivity.class);
                            Bundle bundle = new Bundle();

                            bundle.putStringArrayList("urls", new ArrayList<>(Collections.singletonList(imageUrl)));
                            intent.putExtras(bundle);
                            ((Activity) context).startActivityForResult(intent, 0);*/
                        }
                    });
                }
            }
        }
        return view;
    }

    private View getMessageView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ChatMessage chatMessage = values.get(position);
        MessageViewHolder messageViewHolder = null;
        if (view == null) {
            view = inflater.inflate(R.layout.chat_list_item, parent, false);
            messageViewHolder = new MessageViewHolder();
            messageViewHolder.message = (TextView) view.findViewById(R.id.message);
            messageViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            view.setTag(messageViewHolder);
        } else {
            messageViewHolder = (MessageViewHolder) view.getTag();
        }
        messageViewHolder.message.setText(chatMessage.getMessage());
        try {
            messageViewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }


    /*
       RENDERING OWN VIEW I.E; right side of the chat
     */
    private View getOwnView(int position, View convertView, ViewGroup parent) {
        final ChatMessage chatMessage = values.get(position);
        View view = convertView;
        ViewHolderOwn ownViewHolder  = null;
        if(view == null) {
            view = inflater.inflate(R.layout.chat_list_item_own, parent, false);
            ownViewHolder = new ViewHolderOwn();
            ownViewHolder.message = (TextView) view.findViewById(R.id.message);
            ownViewHolder.timestamp = (TextView) view.findViewById(R.id.timestamp);
            ownViewHolder.messageHolder = view.findViewById(R.id.message_holder);

            ownViewHolder.productLayout = view.findViewById(R.id.product_image_layout);
            ownViewHolder.productImage = (ImageView) view.findViewById(R.id.product_image);
            ownViewHolder.productTitle = (TextView) view.findViewById(R.id.product_title);
            ownViewHolder.productPrice = (TextView) view.findViewById(R.id.product_price);

            view.setTag(ownViewHolder);
        }else{
            ownViewHolder = (ViewHolderOwn) view.getTag();
        }

        ownViewHolder.messageHolder.setOnClickListener(messageSendListener);

        if(chatMessage.getBitmap() != null){
            ownViewHolder.productLayout.setVisibility(View.VISIBLE);
            ownViewHolder.message.setVisibility(GONE);
            ownViewHolder.productPrice.setVisibility(GONE);
            ownViewHolder.productTitle.setVisibility(GONE);
            ownViewHolder.productImage.setVisibility(View.VISIBLE);
            ownViewHolder.productImage.setImageBitmap(chatMessage.getBitmap());
            ownViewHolder.productImage.setOnClickListener(null);
        }else{
            ownViewHolder.message.setVisibility(View.VISIBLE);
            ownViewHolder.productLayout.setVisibility(GONE);

            MessageObject messageObject = null;
            try{
                messageObject = gson.fromJson(chatMessage.getMessage(), MessageObject.class);
            }catch (Exception e){
                //e.printStackTrace();
            }

            if(messageObject!=null && (messageObject.getMessage()!=null || messageObject.getImage()!=null)){
                ownViewHolder.message.setVisibility(GONE);

                String title = messageObject.getMessage();
                if(title != null){
                    ownViewHolder.productTitle.setText(title);
                    ownViewHolder.productTitle.setVisibility(View.VISIBLE);
                }else{
                    ownViewHolder.productTitle.setVisibility(GONE);
                }
                if(messageObject.getPriceString() != null){
                    ownViewHolder.productPrice.setText(messageObject.getPriceString());
                    ownViewHolder.productPrice.setVisibility(View.VISIBLE);
                }else{
                    ownViewHolder.productPrice.setVisibility(GONE);
                }

                if(messageObject.getImage()!=null){
                    ownViewHolder.productImage.setVisibility(View.VISIBLE);
                    DrawableManager.getInstance(context).fetchDrawableOnThread(messageObject.getImage(), ownViewHolder.productImage);

                    final String imageUrl = messageObject.getImage();
                    ownViewHolder.productImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ImageActivity.class);
                            Bundle bundle = new Bundle();

                            bundle.putStringArrayList("urls", new ArrayList<>(Collections.singletonList(imageUrl)));
                            intent.putExtras(bundle);
                            ((Activity) context).startActivityForResult(intent, 0);
                        }
                    });
                }else{
                    ownViewHolder.productImage.setVisibility(GONE);
                }
                ownViewHolder.productLayout.setVisibility(View.VISIBLE);
            }else {
                ownViewHolder.productLayout.setVisibility(GONE);
                ownViewHolder.message.setVisibility(View.VISIBLE);
                ownViewHolder.timestamp.setVisibility(View.VISIBLE);
                ownViewHolder.message.setText(Html.fromHtml(chatMessage.getMessage()));
            }
        }

        String timestampText = null;
        try {
            timestampText = DateUtils.getRelativeTimeSpanString(format.parse(values.get(position).getTimestamp()).getTime(),(new Date()).getTime(),DateUtils.FORMAT_ABBREV_RELATIVE).toString()+" - "+(chatMessage.getAcknowledged()?"Sent":"Sending..");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ownViewHolder.timestamp.setText(timestampText);
        return view;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        EventBus.getDefault().post(new ChatUpdatedEvent(true));
    }

    @Override
    public int getViewTypeCount() {
        return 8;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = values.get(position);
        return chatMessage.getViewType();
    }

    public void setName(String name){
        this.name = name;
    }

    View.OnClickListener messageSendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // EventBus.getDefault().post(new LoginEvent());
        }
    };

}
