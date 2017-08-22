package com.yellowmessenger.sdk.models;

import java.io.Serializable;
import java.util.List;

public class Option implements Serializable{
    private String value;
    private String label;

    private String image;

    private List<Question> questions;
    private boolean location = false;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public Option() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
