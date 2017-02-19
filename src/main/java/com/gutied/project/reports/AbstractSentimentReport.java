package com.gutied.project.reports;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.datasets.SentimentRange;
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

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.*;

abstract public class AbstractSentimentReport {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSentimentReport.class);

    private long[] counters;

    public AbstractSentimentReport() {
        counters = new long[OpinionRange.values().length];
        Arrays.stream(OpinionRange.values()).forEach(x -> counters[x.ordinal()] = 0);
    }

    protected void createSentimentReport(String provider, String filename, String sentimentDocument, String scoreDocument, String
            magnitudeDocument) throws IOException {
        long[] matches = new long[SentimentRange.values().length];
        long[] noMatches = new long[SentimentRange.values().length];
        long objectsWhereSentimentAnalysisWasNotConclusive = 0;
        Arrays.stream(SentimentRange.values()).forEach(x -> {
            matches[x.ordinal()] = 0;
            noMatches[x.ordinal()] = 0;
        });

        List<DBObject> allQuotes = MongoDB.getSentimentDataForAllQuotes(sentimentDocument);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_16)) {
            boolean match = false;
            writer.write("Review id, Tripadvisor quote, Tripadvisor rank, " + provider + " score, " + provider + " magnitude, Tripadvisor" + " " +
                    "sentiment, " + provider + " sentiment, Match\n");
            for (DBObject reviewQuote : allQuotes) {
                String comment = (String) reviewQuote.get(quote.toString());
                Double tripAdvisorRank = (Double) reviewQuote.get(rank.toString());
                String reviewIdentifier = (String) reviewQuote.get(reviewId.toString());
                SentimentRange providerSentimentType = null;
                SentimentRange tripadvisorSentimentType;
                DBObject providerSentimentObject = (DBObject) reviewQuote.get(sentimentDocument);
                Double providerSentimentScore;
                Double providerSentimentMagnitude;
                tripadvisorSentimentType = SentimentRange.getTripAdvisorRange(tripAdvisorRank);
                if (containsSentimentEntry(providerSentimentObject)) {
                    providerSentimentScore = (Double) providerSentimentObject.get(scoreDocument);
                    providerSentimentMagnitude = magnitudeDocument != null ? (Double) providerSentimentObject.get(magnitudeDocument) : 0;
                    providerSentimentType = getSentimentRange(providerSentimentScore);
                    if (providerSentimentType == tripadvisorSentimentType) {
                        matches[tripadvisorSentimentType.ordinal()]++;
                        match = true;
                    } else {
                        noMatches[tripadvisorSentimentType.ordinal()]++;
                        match = false;
                        LOG.info("No match Tripadvisor: {} {} " + provider + " {} {} - {}", tripadvisorSentimentType.name(),
                                tripAdvisorRank, providerSentimentType.name(), providerSentimentScore, comment);
                    }
                } else {
                    objectsWhereSentimentAnalysisWasNotConclusive++;
                    providerSentimentScore = 0.0;
                    providerSentimentMagnitude = 0.0;
                }
                writer.write(createSentimentReportLine(match, comment, tripAdvisorRank, reviewIdentifier, providerSentimentType,
                        tripadvisorSentimentType, providerSentimentObject, providerSentimentScore, providerSentimentMagnitude));
            }
        }
        Arrays.stream(SentimentRange.values()).forEach(x -> LOG.info("{} Matches: {}  No matches: {}", x.name(), matches[x.ordinal()],
                noMatches[x.ordinal()]));
        LOG.info("Quotes where sentiment Analysis was not conclusive {}", objectsWhereSentimentAnalysisWasNotConclusive);

    }




    protected String createSentimentReportLine(boolean match, String comment, Double tripAdvisorRank, String reviewIdentifier,
                                               SentimentRange providerSentimentType, SentimentRange tripadvisorSentimentType, DBObject
                                                       providerSentimentObject, Double providerSentimentScore, Double
                                                       providerSentimentMagnitude) {
        StringBuffer sb = new StringBuffer();
        sb.append(reviewIdentifier).append(", ");
        sb.append(comment.replaceAll(",", "").replaceAll("“", "").replaceAll("”", "")).append(", ");
        sb.append(tripAdvisorRank).append(", ");
        sb.append(providerSentimentScore).append(", ");
        sb.append(providerSentimentMagnitude).append(", ");
        sb.append(tripadvisorSentimentType.name()).append(", ");
        if (containsSentimentEntry(providerSentimentObject)) {
            sb.append(providerSentimentType.name()).append(", ");
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

    abstract protected boolean containsSentimentEntry(DBObject providerSentimentObject);

    abstract protected SentimentRange getSentimentRange(Double providerSentimentScore);


}
