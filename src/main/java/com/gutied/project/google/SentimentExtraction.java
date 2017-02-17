package com.gutied.project.google;


import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.mongodb.GoogleLanguageApiDbParser;
import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.gutied.project.mongodb.HotelReviewDbParser;
import com.mongodb.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Application that reads all the hotel reviews for a given city from the database and invokes Google's
 * Natural Language Api to analyse the semtiment of the quote of the hotel's review.
 * <p>
 * <p>The application stores the results in the database.
 * <p>
 * <p>In order to work the GOOGLE_APPLICATION_CREDENTIALS environment variable needs to be set pointing to the
 * json file containing Google's API key.
 * {@link https://developers.google.com/identity/protocols/application-default-credentials}
 */
public class SentimentExtraction {

    private static Logger LOG = LoggerFactory.getLogger(SentimentExtraction.class);

    private final LanguageServiceClient languageApi;

    private static int googleApiCallCounter = 0;

    public SentimentExtraction() throws IOException {
        languageApi = LanguageServiceClient.create();
    }

    /**
     * {@code findAndSaveSentimentForAllReviewsInCity} finds all TripAdvisor reviews in the db, invokes a sentiment
     * analysis function for the review's quote and stores the result in the db.
     *
     * @param city The city for which reviews analyze.
     */
    public void findAndSaveSentimentForAllReviewsInCity(String city) {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(HotelReviewDbParser.tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.googleSentiment.toString(), new BasicDBObject("$exists", false));
        query.put(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.city.toString(), city);
        DBCursor cursor = hotelReviewCollection.find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        for (DBObject hotelReviewDbObject : cursor) {
            DBObject googleSentimentDBObject = extractReviewTextAndGetSentiment(hotelReviewDbObject);
            if (googleSentimentDBObject != null) {
                hotelReviewCollection.update(hotelReviewDbObject, new BasicDBObject("$set", new BasicDBObject(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.googleSentiment.toString(), googleSentimentDBObject)), false, false);
                LOG.info("[{}] Updated hotel [{}] with quote [{}] sentiment data: [{}]", googleApiCallCounter, hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName.toString()), hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.quote.toString()), googleSentimentDBObject);
            }
        }
    }

    /**
     * {@code extractReviewTextAndGetSentiment} Invokes a sentiment analysis function for the review passed as parameter
     * and returns the result as a mongo Db object.
     * <p>
     * <p>It doesn't invoke the sentiment analysis if the review {@link DBObject} already has the sentiment fields populated
     * to avoid making extra calls to the analysis API.
     *
     * @param hotelReviewDbObject A mongo db object containing the fields of a TripAdvisor review.
     * @return {@link DBObject} with the  result data of the sentiment analysis call.
     * {@link GoogleLanguageApiDbParser#parseSentiment}
     */
    private DBObject extractReviewTextAndGetSentiment(DBObject hotelReviewDbObject) {
        String quote = (String) hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.quote.toString());
        if (Strings.isNotEmpty(quote)) {
            Object googleSentiment = hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.googleSentiment.toString());
            if (googleSentiment == null) {
                return analyzeSentiment(quote);
            } else {
                DBObject googleSentimentDBObject = (DBObject) googleSentiment;
                LOG.info("Review for hotel [{}] already has Google sentiment data [{}]", hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName.toString()), googleSentimentDBObject);
                return googleSentimentDBObject;
            }
        }
        return null;
    }

    /**
     * {@code analyzeEntities} invokes Google's Natural Language Api sentiment analysis for the given string.
     * <p>
     * <p>If an error is returned from the analysis api is added to the {@link DBObject}
     *
     * @param sentence Text to analyze.
     * @return {@link DBObject} containing the result's data.
     * {@link GoogleLanguageApiDbParser#parseSentiment}. Returns [@code null} if an exception is raised when invoking
     * Google's API.
     */
    private DBObject analyzeSentiment(String sentence) {
        Document doc = Document.newBuilder().setContent(sentence).setType(Document.Type.PLAIN_TEXT).build();
        try {
            AnalyzeSentimentResponse response = languageApi.analyzeSentiment(doc);
            googleApiCallCounter++;
            return GoogleLanguageApiDbParser.parseSentiment(response);
        } catch (Exception e) {
            LOG.error("Exception on call to Google API [{}]", e.getMessage());
            return new BasicDBObject("error", e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            SentimentExtraction sentimentExtraction = new SentimentExtraction();
            sentimentExtraction.findAndSaveSentimentForAllReviewsInCity(args[0]);
        } else {
            System.out.println("Enter the name of a city.");
        }
    }

}
