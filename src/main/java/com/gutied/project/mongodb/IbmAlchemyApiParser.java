package com.gutied.project.mongodb;

import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class IbmAlchemyApiParser {

    public enum KeywordCollectionKeys {error, keyword, relevance, sentiment, sentimentScore}

    public static List<DBObject> parseKeywords(Keywords keywords) {
        List<DBObject> result = new ArrayList<>(keywords.getKeywords().size());
        for (Keyword keyword : keywords.getKeywords()) {
            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put(KeywordCollectionKeys.keyword.toString(), keyword.getText());
            basicDBObject.put(KeywordCollectionKeys.relevance.toString(), keyword.getRelevance());
            basicDBObject.put(KeywordCollectionKeys.sentiment.toString(), keyword.getSentiment().getType().toString());
            basicDBObject.put(KeywordCollectionKeys.sentimentScore.toString(), keyword.getSentiment().getScore());
            result.add(basicDBObject);
        }
        return result;

    }
}
