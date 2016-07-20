package com.yellowmessenger.sdk.models;

import android.text.InputType;

public enum FieldType {
    DATETIME,
    PHONE,
    TEXT,
    EMAIL,
    NUMBER,
    DATE,
    CONFIRM,
    ADDRESS,
    TIME;

    public static int getInputType(FieldType fieldType){
        switch (fieldType){
            case DATE:
                return InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_DATE;
            case TIME:
                return InputType.TYPE_CLASS_TEXT;
            case DATETIME:
                return InputType.TYPE_CLASS_DATETIME;
            case PHONE:
                return InputType.TYPE_CLASS_PHONE;
            case NUMBER:
                return InputType.TYPE_CLASS_NUMBER;
            case EMAIL:
                return InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }
}
