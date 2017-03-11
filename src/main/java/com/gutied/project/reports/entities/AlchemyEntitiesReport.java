package com.gutied.project.reports.entities;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.mongodb.HotelReviewDbMapper;
import com.gutied.project.mongodb.IbmAlchemyDbMapper;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.reports.ReportHelper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.gutied.project.reports.ReportHelper.writeResultsToFileOcurrences;
import static com.ibm.watson.developer_cloud.alchemy.v1.model.Sentiment.SentimentType.NEGATIVE;
import static com.ibm.watson.developer_cloud.alchemy.v1.model.Sentiment.SentimentType.POSITIVE;

public class AlchemyEntitiesReport {

    private long[] counters;

    private AlchemyEntitiesReport() {
        counters = new long[OpinionRange.values().length];
        Arrays.stream(OpinionRange.values()).forEach(x -> counters[x.ordinal()] = 0);
    }

    private void createEntitiesReport(String filename) throws IOException {
        SortedMap<String, Long> positiveEntitiesHistogram = new TreeMap<>();
        SortedMap<String, Long> negativeEntitiesHistogram = new TreeMap<>();
        List<DBObject> quotes = MongoDB.getIbmEntitiesAnalysisForAllQuotes();
        for (DBObject quote : quotes) {
            extractPositiveAndNegativeEntitiesForQuote(positiveEntitiesHistogram, negativeEntitiesHistogram, quote);
        }
        writeResultsToFileOcurrences(filename, positiveEntitiesHistogram, negativeEntitiesHistogram);
    }

    private void extractPositiveAndNegativeEntitiesForQuote(SortedMap<String, Long> entitiesHistogram,
                                                            SortedMap<String, Long> negativeEntitiesHistogram,
                                                            DBObject reviewData) {
        BasicDBList allEntities = (BasicDBList) reviewData.get(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys
                .alchemyKeywords.toString());
        if (allEntities != null && allEntities.size() > 0) {
            DBObject error = (DBObject) reviewData.get(IbmAlchemyDbMapper.KeywordCollectionKeys.error.toString());
            if (error == null) {
                for (Object entitiesList : allEntities) {
                    BasicDBObject entitiesObject = (BasicDBObject) entitiesList;
                    String entityName = (String) entitiesObject.get(IbmAlchemyDbMapper.KeywordCollectionKeys.keyword
                            .toString());
                    if (Strings.isNotEmpty(entityName)) {
                        String[] words = ReportHelper.normalizeEntities(entityName);
                        for (String word : words) {
                            word = word.trim();
                            if (word.length() > 2) {
                                if (POSITIVE.name().equals(entitiesObject.get(IbmAlchemyDbMapper
                                        .KeywordCollectionKeys.sentiment.toString()))) {
                                    Long entityCounter = entitiesHistogram.get(word.trim());
                                    entityCounter = entityCounter == null ? 1L : entityCounter + 1;
                                    entitiesHistogram.put(word.trim(), entityCounter);
                                } else if (NEGATIVE.name().equals(entitiesObject.get(IbmAlchemyDbMapper
                                        .KeywordCollectionKeys.sentiment.toString()))) {
                                    Long entityCounter = negativeEntitiesHistogram.get(word.trim());
                                    entityCounter = entityCounter == null ? 1L : entityCounter + 1;
                                    negativeEntitiesHistogram.put(word.trim(), entityCounter);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        AlchemyEntitiesReport quoteDataSet = new AlchemyEntitiesReport();
        quoteDataSet.createEntitiesReport("AlchemyEntitiesReport.csv");
    }

}
