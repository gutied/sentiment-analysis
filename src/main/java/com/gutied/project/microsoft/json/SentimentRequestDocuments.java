package com.gutied.project.microsoft.json;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SentimentRequestDocuments {

    private List<SentimentRequest> documents;

    public List<SentimentRequest> getDocuments() {
        return documents;
    }

    public void setDocuments(List<SentimentRequest> documents) {
        this.documents = documents;
    }

    public void addRequest(SentimentRequest sentimentRequest) {
        if (documents == null) {
            documents = new ArrayList<>(1);
        }
        documents.add(sentimentRequest);
    }
}


