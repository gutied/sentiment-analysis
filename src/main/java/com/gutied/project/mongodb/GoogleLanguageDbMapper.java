package com.gutied.project.mongodb;


import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Entity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class GoogleLanguageDbMapper {

    public enum SentimentCollectionKeys {language, magnitude, score}

    public enum EntitiesCollectionKeys {name, type, salience, offset}

    /**
     * Recieves a response of Googles sentiment analysis and stores each entity in a {link DBObject}
     * <p>
     * <p>The fields saved are the name, salience, type and offset.
     *
     * @param googleResponse
     * @return
     */
    public static List<DBObject> parseEntities(AnalyzeEntitiesResponse googleResponse) {
        List<DBObject> entities = new ArrayList<>();
        for (Entity entity : googleResponse.getEntitiesList()) {
            BasicDBObject entityDbObject = new BasicDBObject();
            entityDbObject.put(EntitiesCollectionKeys.name.toString(), entity.getName());
            entityDbObject.put(EntitiesCollectionKeys.salience.toString(), entity.getSalience());
            entityDbObject.put(EntitiesCollectionKeys.type.toString(), entity.getType().toString());
            entityDbObject.put(EntitiesCollectionKeys.offset.toString(), entity.getMentions(0).getText()
                    .getBeginOffset());
            entities.add(entityDbObject);
        }
        return entities;
    }

    public static DBObject parseSentiment(AnalyzeSentimentResponse googleResponse) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put(SentimentCollectionKeys.language.toString(), googleResponse.getLanguage());
        basicDBObject.put(SentimentCollectionKeys.magnitude.toString(), googleResponse.getDocumentSentiment()
                .getMagnitude());
        basicDBObject.put(SentimentCollectionKeys.score.toString(), googleResponse.getDocumentSentiment().getScore());
        return basicDBObject;

    }


}
