package com.gutied.project.google;


import com.gutied.project.mongodb.HotelReviewDbParser.tripAdvisorReviewCollectionKeys;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.mongodb.GoogleLanguageApiDbParser;
import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.mongodb.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbParser.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbParser.tripAdvisorReviewCollectionKeys.googleEntities;
import static com.gutied.project.mongodb.HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName;
import static com.gutied.project.mongodb.HotelReviewDbParser.tripAdvisorReviewCollectionKeys.review;

/**
 * Application that reads all the hotel reviews for a given city from the database and invokes Google's
 * Natural Language Api entity recognition function to analyze the entities in the hotel's review text.
 * <p>
 * <p>The application stores the results in the database.
 * <p>
 * <p>In order to work the GOOGLE_APPLICATION_CREDENTIALS environment variable needs to be set pointing to the
 * json file containing Google's API key.
 * {@see <a href="https://developers.google.com/identity/protocols/application-default-credentials">Google API
 * credentials</a>}
 */
public class EntitiesExtraction {

    private static Logger LOG = LoggerFactory.getLogger(EntitiesExtraction.class);

    private final LanguageServiceClient languageApi;

    private static int googleApiCallCounter = 0;

    public EntitiesExtraction() throws IOException {
        languageApi = LanguageServiceClient.create();
    }

    /**
     * {@code findAndSaveEntitiesForAllReviewsInCity} finds all TripAdvisor reviews in the db, invokes an entity
     * extraction function for the review and stores the result in the db.
     *
     * @param city The city for which reviews analyze.
     */
    private void findAndSaveEntitiesForAllReviewsInCity(String city) {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(googleEntities.toString(), new BasicDBObject("$exists", false));
        query.put(tripAdvisorReviewCollectionKeys.city.toString(), city);
        DBCursor cursor = hotelReviewCollection.find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        for (DBObject hotelReviewDbObject : cursor) {
            List<DBObject> googleEntitiesDBObject = extractReviewTextAndGetEntities(hotelReviewDbObject);
            if (googleEntitiesDBObject != null) {
                hotelReviewCollection.update(hotelReviewDbObject, new BasicDBObject("$set", new BasicDBObject
                        (googleEntities.toString(), googleEntitiesDBObject)), false, false);
                LOG.info("[{}] Updated hotel [{}] with review [{}] entities data: [{}]", googleApiCallCounter,
                        hotelReviewDbObject.get(hotelName.toString()), hotelReviewDbObject.get(review.toString()),
                        googleEntitiesDBObject);
            }
        }
    }

    /**
     * {@code extractReviewTextAndGetEntities} Invokes an entity extraction function for the review passed as parameter
     * and returns the result as a mongo Db object.
     * <p>
     * <p>It doesn't invoke the entity analysis if the review {@link DBObject} already has the entity fields populated
     * to avoid making extra calls to the analysis API.
     *
     * @param hotelReviewDbObject A mongo db object containing the fields of a TripAdvisor review.
     * @return {@link List<DBObject>} with one entry for each entity found, mapped with
     * {@link GoogleLanguageApiDbParser#parseEntities}
     */
    private List<DBObject> extractReviewTextAndGetEntities(DBObject hotelReviewDbObject) {
        String reviewString = (String) hotelReviewDbObject.get(review.toString());
        if (Strings.isNotEmpty(reviewString)) {
            Object googleEntitiesList = hotelReviewDbObject.get(googleEntities.toString());
            if (googleEntities == null) {
                return analyzeEntities(reviewString);
            } else {
                List<DBObject> googleEntitiesDBObject = (List<DBObject>) googleEntitiesList;
                LOG.info("Review for hotel [{}] already has Google sentiment data [{}]", hotelReviewDbObject.get
                        (hotelName.toString()), googleEntitiesDBObject);
                return googleEntitiesDBObject;
            }
        }
        return null;
    }

    /**
     * {@code analyzeEntities} invokes Google's Natural Language Api entity recognition function to analyze the given
     * string.
     * <p>
     * <p>If an error is returned from the analysis api is added to the {@link DBObject}
     *
     * @param text Text to analyze.
     * @return {@link List<DBObject>} with one entry for each entity found, mapped with
     * {@link GoogleLanguageApiDbParser#parseEntities}. Returns [@code null} if an exception is raised when invoking
     * Google's API.
     */
    private List<DBObject> analyzeEntities(String text) {
        if (Strings.isNotEmpty(text)) {
            Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc).setEncodingType
                    (EncodingType.UTF16).build();
            try {
                AnalyzeEntitiesResponse response = languageApi.analyzeEntities(request);
                googleApiCallCounter++;
                return GoogleLanguageApiDbParser.parseEntities(response);
            } catch (Exception e) {
                LOG.error("Exception on call to Google API [{}]", e.getMessage());
                List<DBObject> errorResponse = new ArrayList<>(1);
                errorResponse.add(new BasicDBObject("error", e.getMessage()));
                return errorResponse;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            EntitiesExtraction sentimentExtraction = new EntitiesExtraction();
            sentimentExtraction.findAndSaveEntitiesForAllReviewsInCity(args[0]);
        } else {
            System.out.println("Enter the name of a city.");
        }
    }

}
