package com.gutied.project.tasks;


import com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys;
import com.gutied.project.mongodb.MongoDB;
import com.mongodb.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.*;

/**
 * Application that reads all the hotel reviews for a given city from the database and invokes a
 * entity recognition function to analyze the entities in the hotel's review text.
 * <p>
 * <p>The application stores the results in the database.
 */
public abstract class AbstractEntitiesAnalysis {

    private static Logger LOG = LoggerFactory.getLogger(AbstractEntitiesAnalysis.class);

    /**
     * {@code findAndSaveEntitiesForAllReviewsInCity} finds all TripAdvisor reviews in the db, invokes an entity
     * extraction function for the review and stores the result in the specified collection.
     *
     * @param city         The city for which reviews analyze.
     * @param documentName Name of the document where to store the sentiment analysis results.
     */
    protected void findAndSaveEntitiesForAllReviewsInCity(String city, String documentName) {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(documentName, new BasicDBObject("$exists", false));
        query.put(tripAdvisorReviewCollectionKeys.city.toString(), city);
        DBCursor cursor = hotelReviewCollection.find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        for (DBObject hotelReviewDbObject : cursor) {
            List<DBObject> entitiesDBObject = extractReviewTextAndGetEntities(hotelReviewDbObject, documentName);
            if (entitiesDBObject != null) {
                hotelReviewCollection.update(hotelReviewDbObject, new BasicDBObject("$set", new BasicDBObject
                        (documentName, entitiesDBObject)), false, false);
                LOG.info("Updated hotel [{}] with review [{}] entities data: [{}]", hotelReviewDbObject.get(hotelName
                        .toString()), hotelReviewDbObject.get(review.toString()), entitiesDBObject);
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
     * @return {@link List<DBObject>} with one entry for each entity found.
     */
    private List<DBObject> extractReviewTextAndGetEntities(DBObject hotelReviewDbObject, String documentName) {
        String reviewString = (String) hotelReviewDbObject.get(review.toString());
        if (Strings.isNotEmpty(reviewString)) {
            Object entitiesList = hotelReviewDbObject.get(documentName);
            if (entities == null) {
                return analyzeEntities(reviewString);
            } else {
                List<DBObject> entitiesDBObject = (List<DBObject>) entitiesList;
                LOG.info("Review for hotel [{}] already has sentiment data [{}]", hotelReviewDbObject.get(hotelName
                        .toString()), entitiesDBObject);
                return entitiesDBObject;
            }
        }
        return null;
    }

    /**
     * {@code analyzeEntities} invokes an entity recognition function to analyze the given string.
     * <p>
     * <p>If an error is returned from the analysis api is added to the {@link DBObject}
     *
     * @param text Text to analyze.
     * @return {@link List<DBObject>} with one entry for each entity found, mapped with
     */
    abstract protected List<DBObject> analyzeEntities(String text);

}
