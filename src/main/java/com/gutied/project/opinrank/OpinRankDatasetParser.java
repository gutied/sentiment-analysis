package com.gutied.project.opinrank;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class OpinRankDatasetParser {

    private static Logger logger = LoggerFactory.getLogger(OpinRankDatasetParser.class);

    private static final String MONGO_DB_SERVER = "localhost";
    private static final String BASE_FOLDER = ".\\";
    private static final Integer MONGO_DB_PORT = 27017;

    private static List<OpinRankHotelReview> allHotelReviews = new LinkedList<>();

    private Mongo mongo;

    private void init() {
        mongo = new Mongo(MONGO_DB_SERVER, MONGO_DB_PORT);
    }

    private void process() {
        init();
        Arrays.stream(listFolders(BASE_FOLDER)).forEach(this::loadAllFilesInFolder);
        DB db = mongo.getDB("reviews_test");
        DBCollection table = db.getCollection("opinRank");
        allHotelReviews.forEach(hotelReview -> table.insert(OpinRankHotelReviewMapper.toDocument
                (hotelReview)));

    }

    private String[] listFolders(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    private void loadAllFilesInFolder(String cityName) {
        File foldersWithReviewsForCity = new File(BASE_FOLDER, cityName);
        String[] hotelReviews = foldersWithReviewsForCity.list();
        Arrays.stream(hotelReviews).forEach(hotelReviewsFileName -> loadAllReviewsFromFile(extractHotelCountry
                (hotelReviewsFileName), cityName, extracHotelName(cityName, hotelReviewsFileName), new File
                (foldersWithReviewsForCity, hotelReviewsFileName)));
    }

    private String extracHotelName(String city, String fileName) {
        String cityWithSpacesInName = city.replace('-', ' ');
        String[] tokens = fileName.split("_");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(city) || tokens[i].equals(cityWithSpacesInName)) {
                StringBuilder sb = new StringBuilder();
                for (int j = i + 1; j < tokens.length; j++) {
                    sb.append(tokens[j]).append(" ");
                }
                return sb.toString().trim();
            }
        }
        return "";
    }

    private String extractHotelCountry(String fileName) {
        return fileName.split("_")[0];
    }

    private void loadAllReviewsFromFile(String country, String cityName, String hotelName, File hotelReviewsFile) {
        logger.info(hotelReviewsFile.getAbsolutePath());
        try (Stream<String> stream = Files.lines(Paths.get(hotelReviewsFile.getAbsolutePath()))) {
            stream.forEach(reviewLine -> allHotelReviews.add(parseReview(country, cityName, hotelName, reviewLine)));
        } catch (Exception e) {
            logger.error("Unable to parse review from file {}", hotelReviewsFile.getName());
        }
    }

    private OpinRankHotelReview parseReview(String country, String cityName, String hotelName, String reviewLine) {
        String[] spittedReviewLine = reviewLine.split(" ");
        String month = spittedReviewLine[0];
        String day = spittedReviewLine[1];
        String year = spittedReviewLine[2];
        int dateLength = 3 + month.length() + day.length() + year.length();
        DateFormat df = new SimpleDateFormat("MMM/dd/yyyy");
        df.setLenient(true);
        Date date = null;
        try {
            date = df.parse(month + "/" + day + "/" + year);
        } catch (ParseException e) {
            logger.error("Unable to parse date for review: {}", reviewLine);
        }
        String review = reviewLine.substring(dateLength).trim();
        logger.info(date + "   " + country + "   '" + cityName + "   '" + hotelName + "'   " + review);
        return new OpinRankHotelReview(date, cityName, country, hotelName, review);
    }

    public static void main(String... args) {
        OpinRankDatasetParser opinRankDatasetLoader = new OpinRankDatasetParser();
        opinRankDatasetLoader.process();
        logger.info("Total reviews created: " + allHotelReviews.size());
    }

}
