package com.gutied.project.microsoft;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gutied.project.microsoft.helper.AzureRequestHelper;
import com.gutied.project.microsoft.json.EntitiesResponseDocuments;
import com.gutied.project.mongodb.AzureDbMapper;
import com.gutied.project.tasks.AbstractEntitiesAnalysis;
import com.mongodb.DBObject;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.azureEntities;


public class AzureEntitiesAnalysis extends AbstractEntitiesAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(AzureEntitiesAnalysis.class);


    private static final String AZURE_SENTIMENT_URL = "https://westus.api.cognitive.microsoft.com/text/analytics/v2.0/keyPhrases";
    private static final String AZURE_API_KEY_HEADER_FIELD = "Ocp-Apim-Subscription-Key";

    private HttpClient httpClient;
    private ObjectMapper mapper;
    private String key = "";

    public AzureEntitiesAnalysis() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        httpClient = new HttpClient(new SslContextFactory());
        try {
            httpClient.start();
        } catch (Exception e) {
            LOG.error("Unable to start HTTP client.", e);
        }
        key = System.getenv("AZURE_KEY");
    }


    private Request createRequest(String text) {
        Request request = httpClient.POST(AZURE_SENTIMENT_URL);
        request.header(AZURE_API_KEY_HEADER_FIELD, key);
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider(AzureRequestHelper.createJSONRequest(mapper, text)), "application/json");
        return request;
    }


    private List<DBObject> sendRequest(String text) {
        if (Strings.isNotEmpty(text) && Strings.isNotEmpty(key)) {
            Request request = createRequest(text);
            ContentResponse response = null;
            try {
                response = request.send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                LOG.error("Error when sending request to Azure", e);
            }
            return getEntitiesFromResponse(response);
        } else {
            LOG.error("We need a key and a text to analyze");
            return null;
        }
    }

    private List<DBObject> getEntitiesFromResponse(ContentResponse response) {
        EntitiesResponseDocuments entitesResponseDocuments = null;
        if (response != null) {
            try {
                entitesResponseDocuments = mapper.readValue(response.getContentAsString(), EntitiesResponseDocuments.class);
            } catch (IOException e) {
                LOG.error("Unable to convert response content to java Object");
            }
        }
        if (entitesResponseDocuments != null && entitesResponseDocuments.getDocuments() != null && entitesResponseDocuments
                .getDocuments().size() == 1) {
            return AzureDbMapper.parseEntities(entitesResponseDocuments.getDocuments().get(0));
        }
        return null;
    }

    public List<DBObject> analyzeEntities(String text) {
        return sendRequest(text);

    }

    public static void main(String[] args) throws IOException {
        AzureEntitiesAnalysis sentimentExtraction = new AzureEntitiesAnalysis();
        sentimentExtraction.findAndSaveEntitiesForAllReviewsInCity("Puerto de la Cruz", azureEntities.toString());
    }

}
