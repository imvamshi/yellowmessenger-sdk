package com.yellowmessenger.sdk.models;

import java.util.List;


public class ChatResponse {
    private Product product;
    private List<Product> products;
    private SearchResults searchResults;
    private Question question;
    private Boolean success;
    private DeepLink deepLink;
    private Boolean typing;
    private Location location;
    private Event event;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SearchResults getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SearchResults searchResults) {
        this.searchResults = searchResults;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public boolean isValid() {
        return getProduct()!=null || getSearchResults()!=null || getQuestion()!=null || getDeepLink()!=null || getEvent()!=null;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public DeepLink getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(DeepLink deepLink) {
        this.deepLink = deepLink;
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
