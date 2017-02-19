package com.gutied.project.tasks;


import com.gutied.project.datasets.SentimentRange;
import com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys;
import com.gutied.project.mongodb.MongoDB;
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

public class CreateTrainingSets {


    private void createTrainingSets() throws IOException {
        List<String> positiveQuotes = new ArrayList();
        List<String> negativeQuotes = new ArrayList();

        DBCursor quotesCursor = MongoDB.getAllQuotes();
        for (DBObject quoteObject  : quotesCursor ) {
            String quote = (String) quoteObject.get(tripAdvisorReviewCollectionKeys.quote.toString());
            Double rank = (Double) quoteObject.get(tripAdvisorReviewCollectionKeys.rank.toString());
            if (Strings.isNotEmpty(quote) && rank != null) {
                quote = normalizeEntities(quote) + "\n";
                SentimentRange quoteSentiment = SentimentRange.getTripAdvisorRange(rank);
                if (SentimentRange.positive.equals(quoteSentiment)) {
                    positiveQuotes.add(quote);
                } else {
                    negativeQuotes.add(quote);
                }
            }
        }
        writeTrainigSetToDisk(positiveQuotes, "PositiveQuotes.txt");
        writeTrainigSetToDisk(negativeQuotes, "NegativeQuotes.txt");
    }

    private void writeTrainigSetToDisk(List<String> positiveQuotes, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), StandardCharsets.UTF_16)) {
            positiveQuotes.stream().forEach((str) -> {
                try {
                    writer.write(str);
                } catch (IOException e) {
                    // Do nothing
                }
            });
        }
    }

    private String normalizeEntities(String string) {
        string = string.toLowerCase().trim();
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
        return string;
    }


    public static void main(String[] args) throws IOException {
        CreateTrainingSets createDataSets = new CreateTrainingSets();
        createDataSets.createTrainingSets();
    }

}
