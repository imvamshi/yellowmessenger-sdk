package com.yellowmessenger.sdk.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Question implements Serializable{
    private FieldType fieldType;
    private String label;
    private String next;
    private String key;
    private String question;
    private List<Option> options;
    private Option parent;
    private String details;
    private String regex;
    private String regexMessage;
    private Boolean save;
    private boolean freeText = true;
    private Option action;
    private boolean autoComplete = false;
    private List<Option> filteredOptions;
    private boolean allowMultipleOptions = false;

    public boolean isAllowMultipleOptions() {
        return allowMultipleOptions;
    }

    public void setAllowMultipleOptions(boolean allowMultipleOptions) {
        this.allowMultipleOptions = allowMultipleOptions;
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public Option getAction() {
        return action;
    }

    public void setAction(Option action) {
        this.action = action;
    }

    public boolean isFreeText() {
        return freeText;
    }

    public void setFreeText(boolean freeText) {
        this.freeText = freeText;
    }

    public Boolean getSave() {
        return save;
    }

    public void setSave(Boolean save) {
        this.save = save;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public Option getParent() {
        return parent;
    }

    public void setParent(Option parent) {
        this.parent = parent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegexMessage() {
        return regexMessage;
    }

    public void setRegexMessage(String regexMessage) {
        this.regexMessage = regexMessage;
    }

    public boolean validate(String value){
        if(regex != null){
            return Pattern.compile(regex).matcher(value).matches();
        }

        switch (fieldType){
            case DATE:
                Matcher dateMatcher = date_pattern.matcher(value);
                return dateMatcher.matches();
            case TIME:
                Matcher timeMatcher = time_pattern.matcher(value);
                Matcher timeMatcher24 = time_24_pattern.matcher(value);
                return timeMatcher.matches() || timeMatcher24.matches();
            case EMAIL:
                Matcher emailMatcher = email_pattern.matcher(value);
                return emailMatcher.matches();
            default:
                return true;
        }
    }

    public List<Option> getFilteredOptions() {
        return filteredOptions;
    }

    public void setFilteredOptions(List<Option> filteredOptions) {
        this.filteredOptions = filteredOptions;
    }

    private static final String time_24_regex = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
    private static final String time_regex = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)";
    private static final String date_regex = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20|)\\d\\d)";
    private static final String email_regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static Pattern time_pattern = Pattern.compile(time_regex);
    private static Pattern time_24_pattern = Pattern.compile(time_24_regex);
    private static Pattern date_pattern = Pattern.compile(date_regex);
    private static Pattern email_pattern = Pattern.compile(email_regex);

    String questionProcessed = null;
    static String pattern = "\\{(\\w+)\\}";
    static Pattern r = Pattern.compile(pattern);

    public String getQuestionProcessed(Map<String,String> data){
        if(questionProcessed == null){
            Matcher m = r.matcher(question);
            questionProcessed = question;
            while(m.find()) {
                String fullKey = m.group(0);
                String key = m.group(1);
                if(data.get(key) != null){
                    questionProcessed = questionProcessed.replace(fullKey,data.get(key));
                }
            }
        }
        return questionProcessed;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
