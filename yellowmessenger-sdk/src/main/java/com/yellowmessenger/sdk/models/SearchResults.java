package com.yellowmessenger.sdk.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SearchResults implements Serializable{
    private List<Product> products;
    private int total;
    private String message;
    private String nextLink;
    private HashMap params;
    private Option action;
    private boolean portraitImage = true;
    private boolean selection = false;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public HashMap getParams() {
        return params;
    }

    public void setParams(HashMap params) {
        this.params = params;
    }

    public Option getAction() {
        return action;
    }

    public void setAction(Option action) {
        this.action = action;
    }

    public boolean isPortraitImage() {
        return portraitImage;
    }

    public void setPortraitImage(boolean portraitImage) {
        this.portraitImage = portraitImage;
    }

    public boolean isSelection() {
        return selection;
    }

    public void setSelection(boolean selection) {
        this.selection = selection;
    }
}
