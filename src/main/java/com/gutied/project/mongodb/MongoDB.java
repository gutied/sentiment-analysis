package com.gutied.project.mongodb;

import com.mongodb.*;

import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.*;

public class MongoDB {

    private static final String PROJECT_DB_NAME = "sentiment_analysis";
    private static final String MONGO_DB_SERVER = "localhost";
    private static final Integer MONGO_DB_PORT = 27017;

    private static Mongo mongo;

    private static void init() {
        mongo = new Mongo(MONGO_DB_SERVER, MONGO_DB_PORT);
    }

    public static Mongo getMongo() {
        return mongo;
    }

    public static DB getProjectDB() {
        if (mongo == null) {
            init();
        }
        return mongo.getDB(PROJECT_DB_NAME);
    }

    public static DBCursor getAllQuotes() {
        DB mongoDb = getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(quote.toString(), new BasicDBObject("$exists", true));
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(quote.toString(), 1);
        return hotelReviewCollection.find(query, projection);
    }

    public static List<DBObject> getSentimentDataForAllQuotes(String sentimentCollection) {
        DB mongoDb = getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(sentimentCollection, new BasicDBObject("$exists", true));
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(sentimentCollection, 1);
        projection.put(reviewId.toString(), 1);
        return hotelReviewCollection.find(query, projection).addOption(Bytes.QUERYOPTION_NOTIMEOUT).toArray();
    }

    public static List<DBObject> getGoogleEntitiesAnalysisForAllQuotes() {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(googleEntities.toString(), new BasicDBObject("$exists", true));
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(googleSentiment.toString(), 1);
        projection.put(googleEntities.toString(), 1);
        projection.put(reviewId.toString(), 1);
        return hotelReviewCollection.find(query, projection).addOption(Bytes.QUERYOPTION_NOTIMEOUT).toArray();
    }

    public static List<DBObject> getAzureEntitiesAnalysisForAllQuotes() {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(azureEntities.toString(), new BasicDBObject("$exists", true));
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(azureSentiment.toString(), 1);
        projection.put(azureEntities.toString(), 1);
        projection.put(reviewId.toString(), 1);
        return hotelReviewCollection.find(query, projection).addOption(Bytes.QUERYOPTION_NOTIMEOUT).toArray();
    }

    public static List<DBObject> getIbmEntitiesAnalysisForAllQuotes() {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(alchemyKeywords.toString(), new BasicDBObject("$exists", true));
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(alchemyKeywords.toString(), 1);
        projection.put(reviewId.toString(), 1);
        return hotelReviewCollection.find(query, projection).addOption(Bytes.QUERYOPTION_NOTIMEOUT).toArray();
    }

}
