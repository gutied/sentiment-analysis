package com.gutied.project.google;


import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.gutied.project.mongodb.GoogleLanguageDbMapper;
import com.gutied.project.tasks.AbstractEntitiesAnalysis;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.googleEntities;

/**
 * Application that reads all the hotel reviews for a given city from the database and invokes Google's
 * Natural Language Api entity recognition function to analyze the googleEntities in the hotel's review text.
 * <p>
 * <p>The application stores the results in the database.
 * <p>
 * <p>In order to work the GOOGLE_APPLICATION_CREDENTIALS environment variable needs to be set pointing to the
 * json file containing Google's API key.
 * {@see <a href="https://developers.google.com/identity/protocols/application-default-credentials">Google API
 * credentials</a>}
 */
public class GoogleEntitiesAnalysis extends AbstractEntitiesAnalysis {

    private static Logger LOG = LoggerFactory.getLogger(GoogleEntitiesAnalysis.class);

    private final LanguageServiceClient languageApi;

    private GoogleEntitiesAnalysis() throws IOException {
        languageApi = LanguageServiceClient.create();
    }

    /**
     * {@code analyzeEntities} invokes Google's Natural Language Api entity recognition function to analyze the given
     * string.
     * <p>
     * <p>If an error is returned from the analysis api is added to the {@link DBObject}
     *
     * @param text Text to analyze.
     * @return {@link List<DBObject>} with one entry for each entity found, mapped with
     * {@link GoogleLanguageDbMapper#parseEntities}. Returns [@code null} if an exception is raised when invoking
     * Google's API.
     */
    public List<DBObject> analyzeEntities(String text) {
        if (Strings.isNotEmpty(text)) {
            Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc).setEncodingType
                    (EncodingType.UTF16).build();
            try {
                AnalyzeEntitiesResponse response = languageApi.analyzeEntities(request);
                return GoogleLanguageDbMapper.parseEntities(response);
            } catch (Exception e) {
                LOG.error("Exception on call to Google API [{}]", e.getMessage());
                List<DBObject> errorResponse = new ArrayList<>(1);
                errorResponse.add(new BasicDBObject("error", e.getMessage()));
                return errorResponse;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            GoogleEntitiesAnalysis sentimentExtraction = new GoogleEntitiesAnalysis();
            sentimentExtraction.findAndSaveEntitiesForAllReviewsInCity(args[0], googleEntities.toString());
        } else {
            System.out.println("Enter the name of a city.");
        }
    }

}
