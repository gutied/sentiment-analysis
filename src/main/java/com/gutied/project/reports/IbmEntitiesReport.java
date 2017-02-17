package com.gutied.project.reports;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.mongodb.IbmAlchemyApiParser;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.mongodb.HotelReviewDbParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.gutied.project.reports.ReportHelper.writeResultsToFile;

public class IbmEntitiesReport {

    private long[] counters;

    public IbmEntitiesReport() {
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
        writeResultsToFile(filename, positiveEntitiesHistogram, negativeEntitiesHistogram);
    }

    private void extractPositiveAndNegativeEntitiesForQuote(SortedMap<String, Long> entitiesHistogram, SortedMap<String, Long> negativeEntitiesHistogram, DBObject reviewData) {
        BasicDBList allEntities = (BasicDBList) reviewData.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.alchemyKeywords.toString());
        if (allEntities != null && allEntities.size() > 0) {
            DBObject error = (DBObject) reviewData.get(IbmAlchemyApiParser.KeywordCollectionKeys.error.toString());
            if (error == null) {
                for (Object entitiesList : allEntities) {
                    BasicDBObject entitiesObject = (BasicDBObject) entitiesList;
                    String entityName = (String) entitiesObject.get(IbmAlchemyApiParser.KeywordCollectionKeys.keyword.toString());
                    if (Strings.isNotEmpty(entityName)) {
                        String[] words = normalizeString(entityName);
                        for (String word : words) {
                            word = word.trim();
                            if (word.length() > 2) {
                                if ("POSITIVE".equals(entitiesObject.get(IbmAlchemyApiParser.KeywordCollectionKeys.sentiment.toString()))) {
                                    Long entityCounter = entitiesHistogram.get(word.trim());
                                    entityCounter = entityCounter == null ? new Long(1) : entityCounter + 1;
                                    entitiesHistogram.put(word.trim(), entityCounter);
                                } else  if ("NEGATIVE".equals(entitiesObject.get(IbmAlchemyApiParser.KeywordCollectionKeys.sentiment.toString()))) {
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
    }

    private String[] normalizeString(String string) {
        string = string.toLowerCase().trim();
        string = string.replaceAll(",", " ");
        string = string.replaceAll("\\.", " ");
        string = string.replaceAll("-", " ");
        string = string.replaceAll("/", " ");
        string = string.replaceAll("!", " ");
        string = string.replaceAll("\\+", " ");
        string = string.replaceAll("\\(", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\*", " ");
        string = string.replaceAll("&", " ");
        string = string.replaceAll("£", " ");
        string = string.replaceAll("$", " ");
        string = string.replaceAll("‘", " ");
        string = string.replaceAll("'", " ");
        return string.split(" ");
    }

    public static void main(String[] args) throws IOException {
        IbmEntitiesReport quoteDataSet = new IbmEntitiesReport();
        quoteDataSet.createEntitiesReport("AlchemyEntities.csv");
    }

}
