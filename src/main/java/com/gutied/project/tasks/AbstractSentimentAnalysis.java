package com.gutied.project.tasks;


import com.gutied.project.mongodb.MongoDB;
import com.mongodb.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.*;
import static java.util.regex.Pattern.compile;

public abstract class AbstractSentimentAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSentimentAnalysis.class);

    protected int MAX_API_CALLS_PER_DAY = Integer.MAX_VALUE;

    protected int apiCallCounter = 0;

    /**
     * {@code findAndSaveSentimentForAllReviewsInCity} finds all TripAdvisor reviews in the db, invokes a sentiment
     * analysis function for the review's quote and stores the result in the document.
     *
     * @param cityName     The city for which reviews analyze.
     * @param documentName Name of the document where to store the sentiment analysis results.
     */
    protected void getAndSaveSentimentForAllReviewsInCity(String cityName, String documentName) {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject queryObject = new BasicDBObject(documentName, new BasicDBObject("$exists", false));
        queryObject.put(city.toString(), cityName);
        queryObject.put(date.toString(), compile("2016"));
        DBCursor cursor = hotelReviewCollection.find(queryObject).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        for (DBObject hotelReviewDbObject : cursor) {
            DBObject sentimentDBObject = getHotelReviewSentiment(hotelReviewDbObject, documentName);
            if (sentimentDBObject != null) {
                hotelReviewCollection.update(hotelReviewDbObject, new BasicDBObject("$set", new BasicDBObject
                        (documentName, sentimentDBObject)), false, false);
                LOG.info("[{}] Updated hotel [{}] date [{}] with review [{}] keyword data: [{}]", apiCallCounter,
                        hotelReviewDbObject.get(hotelName.toString()), hotelReviewDbObject.get(date.toString()),
                        hotelReviewDbObject.get(review.toString()), sentimentDBObject);
            }
            if (apiCallCounter > MAX_API_CALLS_PER_DAY) {
                break;
            }
        }
    }

    /**
     * {@code extractReviewTextAndGetSentiment} Invokes a sentiment analysis function for the review passed as parameter
     * and returns the result as a mongo Db object.
     * <p>
     * <p>It doesn't invoke the sentiment analysis if the review {@link DBObject} already has the sentiment fields
     * populated
     * to avoid making extra calls to the analysis API.
     *
     * @param hotelReviewDbObject A mongo db object containing the fields of a TripAdvisor review.
     * @param documentName        Name of the document where to store the sentiment analysis results.
     * @return {@link DBObject} with the  result data of the sentiment analysis call.
     */
    protected DBObject getHotelReviewSentiment(DBObject hotelReviewDbObject, String documentName) {
        String quoteString = (String) hotelReviewDbObject.get(quote.toString());
        if (Strings.isNotEmpty(quoteString)) {
            Object sentimentObject = hotelReviewDbObject.get(documentName);
            if (sentimentObject == null) {
                return analyzeSentiment(quoteString);
            } else {
                DBObject sentimentDBObject = (DBObject) sentimentObject;
                LOG.info("Review for hotel [{}] already has sentiment data [{}]", hotelReviewDbObject.get(hotelName
                        .toString()), sentimentDBObject);
                return sentimentDBObject;
            }
        }
        return null;
    }

    abstract public DBObject analyzeSentiment(String text);

}
