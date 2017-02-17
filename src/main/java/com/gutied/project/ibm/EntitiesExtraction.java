package com.gutied.project.ibm;


import com.gutied.project.mongodb.IbmAlchemyApiParser;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.mongodb.HotelReviewDbParser;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.mongodb.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.regex.Pattern.compile;

public class EntitiesExtraction {

    private static final Logger LOG = LoggerFactory.getLogger(EntitiesExtraction.class);

    private static final int MAX_API_CALLS_PER_DAY = 1000;

    private int apiCallCounter = 0;

    private AlchemyLanguage alchemyLanguage = new AlchemyLanguage();

    public EntitiesExtraction() throws IOException {
        alchemyLanguage.setApiKey(System.getenv("ALCHEMY_API_KEY"));
    }

    public void getAndSaveKeywordsForAllReviewsInCity(String city) {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(HotelReviewDbParser.tripAdvisorReviewCollection);
        DBObject query = new BasicDBObject(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.alchemyKeywords.toString(), new BasicDBObject("$exists", false));
        query.put(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.city.toString(), city);
        query.put(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.date.toString(), compile("2016"));
        DBCursor cursor = hotelReviewCollection.find(query).addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        for (DBObject hotelReviewDbOjbect : cursor) {
            List<DBObject> googleEntitiesDBObject = getHotelReviewKeywords(hotelReviewDbOjbect);
            if (googleEntitiesDBObject != null) {
                hotelReviewCollection.update(hotelReviewDbOjbect, new BasicDBObject("$set", new BasicDBObject(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.alchemyKeywords.toString(), googleEntitiesDBObject)), false, false);
                LOG.info("[{}] Updated hotel [{}] date [{}] with review [{}] keyword data: [{}]", apiCallCounter, hotelReviewDbOjbect.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName.toString()), hotelReviewDbOjbect.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.date.toString()), hotelReviewDbOjbect.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.review.toString()), googleEntitiesDBObject);
            }
            if (apiCallCounter > MAX_API_CALLS_PER_DAY) {
                break;
            }
        }
    }

    private List<DBObject> getHotelReviewKeywords(DBObject hotelReviewDbObject) {
        String review = (String) hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.review.toString());
        if (Strings.isNotEmpty(review)) {
            Object alchemyKeywordsList = hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.alchemyKeywords.toString());
            if (alchemyKeywordsList == null) {
                return analyzeKeywords(review);
            } else {
                List<DBObject> alchemyKeywordsDBObject = (List<DBObject>) alchemyKeywordsList;
                LOG.info("Review for hotel [{}] already has IBM Alchemy keywords data [{}]", hotelReviewDbObject.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName.toString()), alchemyKeywordsDBObject);
                return alchemyKeywordsDBObject;
            }
        }
        return null;
    }


    public List<DBObject> analyzeKeywords(String text) {
        Map<String,Object> params = new HashMap<>();
        params.put(AlchemyLanguage.TEXT, text);
        params.put(AlchemyLanguage.SENTIMENT, 1);
        try {
            Keywords keywords = alchemyLanguage.getKeywords(params).execute();
            apiCallCounter++;
            return IbmAlchemyApiParser.parseKeywords(keywords);
        } catch (Exception e) {
            LOG.error("Exception on call to Google API [{}]", e.getMessage());
//            List<DBObject> errorResponse = new ArrayList<>(1);
//            errorResponse.add(new BasicDBObject("error", e.getMessage()));
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        EntitiesExtraction sentimentExtraction = new EntitiesExtraction();
        sentimentExtraction.getAndSaveKeywordsForAllReviewsInCity("Puerto de la Cruz");
    }

}
