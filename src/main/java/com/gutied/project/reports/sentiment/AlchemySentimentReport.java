package com.gutied.project.reports.sentiment;


import com.gutied.project.datasets.SentimentRange;
import com.mongodb.DBObject;

import java.io.IOException;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.alchemySentiment;
import static com.gutied.project.mongodb.IbmAlchemyDbMapper.KeywordCollectionKeys.relevance;

public class AlchemySentimentReport extends AbstractSentimentReport {

    protected SentimentRange getSentimentRange(Double sentimentScore) {
        return SentimentRange.getAlchemySentimentRange(sentimentScore);
    }

    protected boolean containsSentimentEntry(DBObject sentimentObject) {
        Double sentimentDouble = (Double) sentimentObject.get(relevance.toString());
        return sentimentDouble != null;

    }

    public static void main(String[] args) throws IOException {
        AlchemySentimentReport quoteDataSet = new AlchemySentimentReport();
        quoteDataSet.createSentimentReport("IBM", "AlchemySentimentReport.csv", alchemySentiment.toString(), relevance.toString(), null);
    }

}
