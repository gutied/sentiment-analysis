package com.gutied.project.tasks;


import com.gutied.project.datasets.SentimentRange;
import com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys;
import com.gutied.project.mongodb.MongoDB;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.quote;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.rank;

public class CreateTrainingSets {


    private void createTrainingSets() throws IOException {
        List<String> positiveQuotes = new ArrayList();
        List<String> negativeQuotes = new ArrayList();


        DBObject clause1 = new BasicDBObject(tripAdvisorReviewCollectionKeys.city.toString(), new BasicDBObject("$ne", "Adeje"));
        DBObject clause2 = new BasicDBObject(tripAdvisorReviewCollectionKeys.city.toString(), new BasicDBObject("$ne", "Puerto de la Cruz"));
        BasicDBList or = new BasicDBList();
        or.add(clause1);
        or.add(clause2);
        DBObject query = new BasicDBObject("$and", or);
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);
        projection.put(quote.toString(), 1);

        DBCursor quotesCursor = MongoDB.getProjectDB().getCollection(tripAdvisorReviewCollection).find(query, projection);
        for (DBObject quoteObject  : quotesCursor ) {
            String quote = (String) quoteObject.get(tripAdvisorReviewCollectionKeys.quote.toString());
            Double rank = (Double) quoteObject.get(tripAdvisorReviewCollectionKeys.rank.toString());
            if (Strings.isNotEmpty(quote) && rank != null && (rank == 1 || rank == 5)) {
                quote = normalizeString(quote) + "\n";
                SentimentRange quoteSentiment = SentimentRange.getTripAdvisorRange(rank);
                if (SentimentRange.positive.equals(quoteSentiment)) {
                    positiveQuotes.add(quote);
                } else {
                    negativeQuotes.add(quote);
                }
            }
        }
        writeTrainigSetToDisk(positiveQuotes, "PositiveQuotesTraining.txt");
        writeTrainigSetToDisk(negativeQuotes, "NegativeQuotesTraining.txt");
    }

    private void createEvaluationSets() throws IOException {
        List<String> positiveQuotes = new ArrayList();
        List<String> negativeQuotes = new ArrayList();

        DBObject clause1 = new BasicDBObject(tripAdvisorReviewCollectionKeys.city.toString(), "Puerto de la Cruz");
        DBObject clause2 = new BasicDBObject(tripAdvisorReviewCollectionKeys.city.toString(), "Adeje");
        BasicDBList or = new BasicDBList();
        or.add(clause1);
        or.add(clause2);
        DBObject query = new BasicDBObject("$or", or);

        DBCursor quotesCursor = MongoDB.getAllQuotes();
        for (DBObject quoteObject  : quotesCursor ) {
            String quote = (String) quoteObject.get(tripAdvisorReviewCollectionKeys.quote.toString());
            Double rank = (Double) quoteObject.get(tripAdvisorReviewCollectionKeys.rank.toString());
            if (Strings.isNotEmpty(quote) && rank != null) {
                quote = normalizeString(quote) + "\n";
                SentimentRange quoteSentiment = SentimentRange.getTripAdvisorRange(rank);
                if (SentimentRange.positive.equals(quoteSentiment)) {
                    positiveQuotes.add(quote);
                } else {
                    negativeQuotes.add(quote);
                }
            }
        }
        writeTrainigSetToDisk(positiveQuotes, "PositiveQuotesEval.txt");
        writeTrainigSetToDisk(negativeQuotes, "NegativeQuotesEval .txt");
    }


    private void writeTrainigSetToDisk(List<String> positiveQuotes, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_8)) {
            positiveQuotes.stream().forEach((str) -> {
                try {
                    writer.write(str);
                } catch (IOException e) {
                    // Do nothing
                }
            });
        }
    }

    private String normalizeString(String string) {
        string = string.replaceAll(",", " ");
        string = string.replaceAll("\\.", " ");
        string = string.replaceAll("-", " ");
        string = string.replaceAll("/", " ");
        string = string.replaceAll("\\+", " ");
        string = string.replaceAll("\\(", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\*", " ");
        string = string.replaceAll("&", " ");
        string = string.replaceAll("£", " ");
        string = string.replaceAll("$", " ");
        string = string.replaceAll("‘", "");
        string = string.replaceAll("'", "");
        string = string.replaceAll("\"", "");
        string = string.replaceAll("”", "");
        string = string.replaceAll("“", "");
        string = string.toLowerCase().trim();
        return string;
    }


    public static void main(String[] args) throws IOException {
        CreateTrainingSets createDataSets = new CreateTrainingSets();
        createDataSets.createTrainingSets();
    }

}
