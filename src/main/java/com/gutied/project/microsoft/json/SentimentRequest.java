package com.gutied.project.microsoft.json;


public class SentimentRequest {
    private String language;
    private String id;
    private String text;

    public SentimentRequest(String language, String id, String text) {
        this.language = language;
        this.id = id;
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}


