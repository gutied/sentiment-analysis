package com.gutied.project.reports;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.datasets.SentimentRange;
import com.gutied.project.mongodb.GoogleLanguageDbMapper.SentimentCollectionKeys;
import com.gutied.project.mongodb.MongoDB;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gutied.project.mongodb.GoogleLanguageDbMapper.SentimentCollectionKeys.magnitude;
import static com.gutied.project.mongodb.GoogleLanguageDbMapper.SentimentCollectionKeys.score;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.*;

public class GoogleSentimentReport {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleSentimentReport.class);

    private long[] counters;

    public GoogleSentimentReport() {
        counters = new long[OpinionRange.values().length];
        Arrays.stream(OpinionRange.values()).forEach(x -> counters[x.ordinal()] = 0);
    }

    private void createSentimentReport(String filename) throws IOException {
        long[] matches = new long[SentimentRange.values().length];
        long[] noMatches = new long[SentimentRange.values().length];
        long objectsWhereSentimentAnalysisWasNotConclusive = 0;
        Arrays.stream(SentimentRange.values()).forEach(x -> {
            matches[x.ordinal()] = 0;
            noMatches[x.ordinal()] = 0;
        });

        List<DBObject> allQuotes = MongoDB.getGoogleSentimentDataForAllQuotes();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_16)) {
            boolean match = false;
            writer.write("Review id, Tripadvisor quote, Tripadvisor rank, Google score, Google magnitude, Tripadvisor" + " sentiment, " +
                    "Google sentiment, Match\n");
            for (DBObject reviewQuote : allQuotes) {
                String comment = (String) reviewQuote.get(quote.toString());
                Double tripAdvisorRank = (Double) reviewQuote.get(rank.toString());
                String reviewIdentifier = (String) reviewQuote.get(reviewId.toString());
                SentimentRange googleSentimentType = null;
                SentimentRange tripadvisorSentimentType;
                DBObject googleSentimentObject = (DBObject) reviewQuote.get(googleSentiment.toString());
                Double googleSentimentScore;
                Double googleSentimentMagnitude;
                tripadvisorSentimentType = SentimentRange.getTripAdvisorRange(tripAdvisorRank);
                if (containsGoogleSentiment(googleSentimentObject)) {
                    googleSentimentScore = (Double) googleSentimentObject.get(score.toString());
                    googleSentimentMagnitude = (Double) googleSentimentObject.get(magnitude.toString());
                    googleSentimentType = SentimentRange.getGoogleSentimentRange(googleSentimentScore);
                    if (googleSentimentType == tripadvisorSentimentType) {
                        matches[tripadvisorSentimentType.ordinal()]++;
                        match = true;
                    } else {
                        noMatches[tripadvisorSentimentType.ordinal()]++;
                        match = false;
                        LOG.info("No match Tripadvisor: {} {} Google {} {} - {}", tripadvisorSentimentType.name(), tripAdvisorRank,
                                googleSentimentType.name(), googleSentimentScore, comment);
                    }
                } else {
                    objectsWhereSentimentAnalysisWasNotConclusive++;
                    googleSentimentScore = 0.0;
                    googleSentimentMagnitude = 0.0;
                }
                writer.write(createSentimentReportLine(match, comment, tripAdvisorRank, reviewIdentifier, googleSentimentType,
                        tripadvisorSentimentType, googleSentimentObject, googleSentimentScore, googleSentimentMagnitude));
            }
        }
        Arrays.stream(SentimentRange.values()).forEach(x -> LOG.info("{} Matches: {}  No matches: {}", x.name(), matches[x.ordinal()],
                noMatches[x.ordinal()]));
        LOG.info("Quotes where sentiment Analysis was not conclusive {}", objectsWhereSentimentAnalysisWasNotConclusive);

    }


    private String createSentimentReportLine(boolean match, String comment, Double tripAdvisorRank, String reviewIdentifier,
                                             SentimentRange googleSentimentType, SentimentRange tripadvisorSentimentType, DBObject
                                                     googleSentimentObject, Double googleSentimentScore, Double googleSentimentMagnitude) {
        StringBuffer sb = new StringBuffer();
        sb.append(reviewIdentifier).append(", ");
        sb.append(comment.replaceAll(",", "").replaceAll("“", "").replaceAll("”", "")).append(", ");
        sb.append(tripAdvisorRank).append(", ");
        sb.append(googleSentimentScore).append(", ");
        sb.append(googleSentimentMagnitude).append(", ");
        sb.append(tripadvisorSentimentType.name()).append(", ");
        if (containsGoogleSentiment(googleSentimentObject)) {
            sb.append(googleSentimentType.name()).append(", ");
            if (match) {
                sb.append("true");
            } else {
                sb.append("false");
            }
        } else {
            sb.append("n/a").append(", ");
            sb.append("n/a");
        }
        sb.append("\n");
        return sb.toString();
    }

    private boolean containsGoogleSentiment(DBObject googleSentimentObject) {
        Double sentiment = (Double) googleSentimentObject.get(score.toString());
        Double magnitude = (Double) googleSentimentObject.get(SentimentCollectionKeys.magnitude.toString());
        return sentiment != null && magnitude != null && magnitude != 0;

    }

    public static void main(String[] args) throws IOException {
        GoogleSentimentReport quoteDataSet = new GoogleSentimentReport();
        quoteDataSet.createSentimentReport("GoogleSentiment.csv");
    }

}
