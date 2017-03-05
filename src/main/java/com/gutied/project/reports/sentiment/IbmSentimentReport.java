package com.gutied.project.reports.sentiment;


import com.gutied.project.datasets.SentimentRange;
import com.mongodb.DBObject;

import java.io.IOException;

import static com.gutied.project.mongodb.GoogleLanguageDbMapper.GoogleSentimentCollectionKeys.magnitude;
import static com.gutied.project.mongodb.GoogleLanguageDbMapper.GoogleSentimentCollectionKeys.score;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.alchemySentiment;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.googleSentiment;

public class IbmSentimentReport extends AbstractSentimentReport {


    protected SentimentRange getSentimentRange(Double googleSentimentScore) {
        return SentimentRange.getAlchemySentimentRange(googleSentimentScore);
    }

    protected boolean containsSentimentEntry(DBObject googleSentimentObject) {
        Double sentimentDouble = (Double) googleSentimentObject.get(score.toString());
        Double magnitudeDouble = (Double) googleSentimentObject.get(magnitude.toString());
        return sentimentDouble != null && magnitudeDouble != null && magnitudeDouble != 0;

    }

    public static void main(String[] args) throws IOException {
        IbmSentimentReport quoteDataSet = new IbmSentimentReport();
        quoteDataSet.createSentimentReport("IBM", "IbmSentimentReport.csv", alchemySentiment.toString(), magnitude.toString(), magnitude
                .toString());
    }

}
