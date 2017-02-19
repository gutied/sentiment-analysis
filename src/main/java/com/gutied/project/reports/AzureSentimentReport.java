package com.gutied.project.reports;


import com.gutied.project.datasets.SentimentRange;
import com.mongodb.DBObject;

import java.io.IOException;

import static com.gutied.project.mongodb.AzureDbMapper.AzureKeywordCollectionKeys.sentimentScore;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.azureSentiment;

public class AzureSentimentReport extends AbstractSentimentReport {

    protected SentimentRange getSentimentRange(Double providerSentimentScore) {
        return SentimentRange.getAzureSentimentRange(providerSentimentScore);
    }

    protected boolean containsSentimentEntry(DBObject azureSentimentDocument) {
        Double sentiment = (Double) azureSentimentDocument.get(sentimentScore.toString());
        return sentiment != null;

    }

    public static void main(String[] args) throws IOException {
        AzureSentimentReport quoteDataSet = new AzureSentimentReport();
        quoteDataSet.createSentimentReport("Azure", "AzureSentiment.csv", azureSentiment.toString(), sentimentScore.toString(), null);
    }

}
