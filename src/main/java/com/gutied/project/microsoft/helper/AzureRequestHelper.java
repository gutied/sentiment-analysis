package com.gutied.project.microsoft.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gutied.project.microsoft.json.SentimentRequest;
import com.gutied.project.microsoft.json.SentimentRequestDocuments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by David on 19/02/2017.
 */
public class AzureRequestHelper {

    private static final Logger LOG = LoggerFactory.getLogger(AzureRequestHelper.class);

    public static String createJSONRequest(ObjectMapper mapper, String text) {
        String json = "";
        SentimentRequestDocuments sentimentRequestDocuments = new SentimentRequestDocuments();
        sentimentRequestDocuments.addRequest(new SentimentRequest("en", "1", text));
        try {
            return mapper.writeValueAsString(sentimentRequestDocuments);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to create json.", e);
        }
        return json;
    }


}
