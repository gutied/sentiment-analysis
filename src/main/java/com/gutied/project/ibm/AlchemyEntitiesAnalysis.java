package com.gutied.project.ibm;


import com.gutied.project.mongodb.IbmAlchemyDbMapper;
import com.gutied.project.tasks.AbstractEntitiesAnalysis;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.alchemyKeywords;

public class AlchemyEntitiesAnalysis extends AbstractEntitiesAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(AlchemyEntitiesAnalysis.class);

    private AlchemyLanguage alchemyLanguage = new AlchemyLanguage();

    private AlchemyEntitiesAnalysis() throws IOException {
        alchemyLanguage.setApiKey(System.getenv("ALCHEMY_API_KEY"));
    }

    public List<DBObject> analyzeEntities(String text) {
        Map<String, Object> params = new HashMap<>();
        params.put(AlchemyLanguage.TEXT, text);
        params.put(AlchemyLanguage.SENTIMENT, 1);
        try {
            Keywords keywords = alchemyLanguage.getKeywords(params).execute();
            return IbmAlchemyDbMapper.parseKeywords(keywords);
        } catch (Exception e) {
            LOG.error("Exception on call to Google API [{}]", e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        AlchemyEntitiesAnalysis sentimentExtraction = new AlchemyEntitiesAnalysis();
        sentimentExtraction.findAndSaveEntitiesForAllReviewsInCity("Puerto de la Cruz", alchemyKeywords.toString());
    }

}
