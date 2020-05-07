package com.darien.textreader.models;

public class TextSaved {
    private String text;
    private String timestamp;

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public TextSaved(String text, String timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }
}
