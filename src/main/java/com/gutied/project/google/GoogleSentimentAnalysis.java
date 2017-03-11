package com.gutied.project.google;


import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.gutied.project.mongodb.GoogleLanguageDbMapper;
import com.gutied.project.tasks.AbstractSentimentAnalysis;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.googleSentiment;

/**
 * Application that reads all the hotel reviews for a given city from the database and invokes Google's
 * Natural Language Api to analyse the sentiment of the quote of the hotel's review.
 * <p>
 * <p>The application stores the results in the database.
 * <p>
 * <p>In order to work the GOOGLE_APPLICATION_CREDENTIALS environment variable needs to be set pointing to the
 * json file containing Google's API key.
 * {@see https://developers.google.com/identity/protocols/application-default-credentials}
 */
public class GoogleSentimentAnalysis extends AbstractSentimentAnalysis {

    private static Logger LOG = LoggerFactory.getLogger(GoogleSentimentAnalysis.class);

    private final LanguageServiceClient languageApi;

    private GoogleSentimentAnalysis() throws IOException {
        languageApi = LanguageServiceClient.create();
    }

    /**
     * {@code analyzeEntities} invokes Google's Natural Language Api sentiment analysis for the given string.
     * <p>
     * <p>If an error is returned from the analysis api is added to the {@link DBObject}
     *
     * @param text Text to analyze.
     * @return {@link DBObject} containing the result's data.
     * {@link GoogleLanguageDbMapper#parseSentiment}. Returns [@code null} if an exception is raised when invoking
     * Google's API.
     */
    public DBObject analyzeSentiment(String text) {
        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
        try {
            AnalyzeSentimentResponse response = languageApi.analyzeSentiment(doc);
            apiCallCounter++;
            return GoogleLanguageDbMapper.parseSentiment(response);
        } catch (Exception e) {
            LOG.error("Exception on call to Google API [{}]", e.getMessage());
            return new BasicDBObject("error", e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            GoogleSentimentAnalysis sentimentExtraction = new GoogleSentimentAnalysis();
            sentimentExtraction.getAndSaveSentimentForAllReviewsInCity(args[0], googleSentiment.toString());
        } else {
            System.out.println("Enter the name of a city.");
        }
    }

}
