package com.gutied.project.mongodb;

import com.gutied.project.microsoft.json.EntitiesResponse;
import com.gutied.project.microsoft.json.SentimentResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class AzureDbMapper {

    public enum AzureKeywordCollectionKeys {error, sentimentScore}

    public enum AzureEntitiesCollectionKeys {name}

    public static DBObject parseKeywords(SentimentResponse sentiment) {
        if (sentiment.getScore() != null) {
            BasicDBObject result = new BasicDBObject();
            result.put(AzureKeywordCollectionKeys.sentimentScore.toString(), sentiment.getScore());
            return result;
        }
        return null;
    }

    public static List<DBObject> parseEntities(EntitiesResponse googleResponse) {
        List<DBObject> entities = new ArrayList<>();
        for (String entity : googleResponse.getKeyPhrases()) {
            BasicDBObject entityDbObject = new BasicDBObject();
            entityDbObject.put(AzureEntitiesCollectionKeys.name.toString(), entity);
            entities.add(entityDbObject);
        }
        return entities;
    }

}
