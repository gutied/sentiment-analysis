package com.gutied.project.mongodb;


import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Entity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

public class GoogleLanguageDbMapper {

    public enum GoogleSentimentCollectionKeys {language, magnitude, score}

    public enum GoogleEntitiesCollectionKeys {name, type, salience, offset}

    /**
     * Recieves a response of Googles sentiment analysis and stores each entity in a {link DBObject}
     * <p>
     * <p>The fields saved are the name, salience, type and offset.
     *
     * @param googleResponse Object returned by Google entity extraction function.
     * @return A document containing hte data of the entities extraction call.
     */
    public static List<DBObject> parseEntities(AnalyzeEntitiesResponse googleResponse) {
        List<DBObject> entities = new ArrayList<>();
        for (Entity entity : googleResponse.getEntitiesList()) {
            BasicDBObject entityDbObject = new BasicDBObject();
            entityDbObject.put(GoogleEntitiesCollectionKeys.name.toString(), entity.getName());
            entityDbObject.put(GoogleEntitiesCollectionKeys.salience.toString(), entity.getSalience());
            entityDbObject.put(GoogleEntitiesCollectionKeys.type.toString(), entity.getType().toString());
            entityDbObject.put(GoogleEntitiesCollectionKeys.offset.toString(), entity.getMentions(0).getText()
                    .getBeginOffset());
            entities.add(entityDbObject);
        }
        return entities;
    }

    public static DBObject parseSentiment(AnalyzeSentimentResponse googleResponse) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put(GoogleSentimentCollectionKeys.language.toString(), googleResponse.getLanguage());
        basicDBObject.put(GoogleSentimentCollectionKeys.magnitude.toString(), googleResponse.getDocumentSentiment()
                .getMagnitude());
        basicDBObject.put(GoogleSentimentCollectionKeys.score.toString(), googleResponse.getDocumentSentiment().getScore());
        return basicDBObject;

    }


}
