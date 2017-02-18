package com.gutied.project.ibm;


import com.gutied.project.mongodb.IbmAlchemyDbMapper;
import com.gutied.project.tasks.AbstractSentimentAnalysis;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.alchemySentiment;

public class AlchemySentimentAnalysis extends AbstractSentimentAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(AlchemySentimentAnalysis.class);

    private AlchemyLanguage alchemyLanguage = new AlchemyLanguage();

    public AlchemySentimentAnalysis() throws IOException {
        MAX_API_CALLS_PER_DAY = 1000;
        alchemyLanguage.setApiKey(System.getenv("ALCHEMY_API_KEY"));
    }

    public DBObject analyzeSentiment(String text) {
        Map<String, Object> params = new HashMap<>();
        params.put(AlchemyLanguage.TEXT, text);
        try {
            DocumentSentiment sentiment = alchemyLanguage.getSentiment(params).execute();
            apiCallCounter++;
            return IbmAlchemyDbMapper.parseKeywords(sentiment);
        } catch (Exception e) {
            LOG.error("Exception on call to Google API [{}]", e.getMessage());
//            List<DBObject> errorResponse = new ArrayList<>(1);
//            errorResponse.add(new BasicDBObject("error", e.getMessage()));
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        AlchemySentimentAnalysis sentimentExtraction = new AlchemySentimentAnalysis();
        sentimentExtraction.getAndSaveSentimentForAllReviewsInCity("Puerto de la Cruz", alchemySentiment.toString());
    }

}
