package com.example.ikram.myapplicationinf8405;

// Tag Value Pair:
public class TagValuePair {
    private String tag;
    private String value;

    public TagValuePair(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return tag;
    }
}
