package com.gutied.project.reports.entities;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.datasets.SentimentRange;
import com.gutied.project.mongodb.GoogleLanguageDbMapper;
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

import static com.gutied.project.mongodb.AzureDbMapper.AzureEntitiesCollectionKeys.name;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.azureEntities;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.azureSentiment;
import static com.gutied.project.reports.ReportHelper.writeResultsToFile;

public class AzureEntitiesReport {

    private long[] counters;

    public AzureEntitiesReport() {
        counters = new long[OpinionRange.values().length];
        Arrays.stream(OpinionRange.values()).forEach(x -> counters[x.ordinal()] = 0);
    }

    private void createEntitiesReport(String filename) throws IOException {
        SortedMap<String, Long> positiveEntitiesHistogram = new TreeMap<>();
        SortedMap<String, Long> negativeEntitiesHistogram = new TreeMap<>();
        List<DBObject> quotes = MongoDB.getAzureEntitiesAnalysisForAllQuotes();
        for (DBObject quote : quotes) {
            extractPositiveAndNegativeEntitiesForQuote(positiveEntitiesHistogram, negativeEntitiesHistogram, quote);
        }
        writeResultsToFile(filename, positiveEntitiesHistogram, negativeEntitiesHistogram);
    }

    private void extractPositiveAndNegativeEntitiesForQuote(SortedMap<String, Long> entitiesHistogram, SortedMap<String, Long>
            negativeEntitiesHistogram, DBObject quote) {
        BasicDBList allEntities = (BasicDBList) quote.get(azureEntities.toString());
        if (allEntities != null && allEntities.size() > 0) {
            DBObject azureSentimentObject = (DBObject) quote.get(azureSentiment.toString());
            SentimentRange range = getAzureSentimentRange(azureSentimentObject);
            for (Object entitiesList : allEntities) {
                BasicDBObject entitiesObject = (BasicDBObject) entitiesList;
                String entityName = (String) entitiesObject.get(name.toString());
                if (Strings.isNotEmpty(entityName)) {
                    entityName = entityName.toLowerCase().trim();
                    String[] words = ReportHelper.normalizeEntities(entityName);
                    for (String word : words) {
                        word = word.trim();
                        if (word.length() > 2) {
                            if (range == null || range.equals(SentimentRange.positive)) {
                                Long entityCounter = entitiesHistogram.get(word.trim());
                                entityCounter = entityCounter == null ? new Long(1) : entityCounter + 1;
                                entitiesHistogram.put(word.trim(), entityCounter);
                            } else {
                                Long entityCounter = negativeEntitiesHistogram.get(word.trim());
                                entityCounter = entityCounter == null ? new Long(1) : entityCounter + 1;
                                negativeEntitiesHistogram.put(word.trim(), entityCounter);
                            }
                        }
                    }
                }
            }
        }
    }

    private SentimentRange getAzureSentimentRange(DBObject googleSentimentObject) {
        SentimentRange range = null;
        if (googleSentimentObject != null) {
            Double scoreValue = (Double) googleSentimentObject.get(GoogleLanguageDbMapper.GoogleSentimentCollectionKeys.score.toString());
            if (scoreValue != null) {
                range = SentimentRange.getGoogleSentimentRange(scoreValue);
            }
        }
        return range;
    }

    public static void main(String[] args) throws IOException {
        AzureEntitiesReport quoteDataSet = new AzureEntitiesReport();
        quoteDataSet.createEntitiesReport(args[0]);
    }

}
