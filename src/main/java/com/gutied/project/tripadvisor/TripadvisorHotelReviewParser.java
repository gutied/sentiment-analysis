package com.gutied.project.tripadvisor;

import com.gutied.project.Utils;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.mongodb.HotelReviewDbParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This utility reads Tripadvisor reviews in html format extracts information form storing it into a {@code DbObject}
 * and stores it in a MongoDb collection.
 * <p>
 * <p>The reviews are expected to  be stored under a folder structure following the convection
 * ${reviewsPath}/com/${cityId}/${hotelId}/${reviewId}.html where
 * ${}reviewsPath} is passed as a parameter to the main method.
 * <p>
 * <p>The fields extracted from the review are:
 * - hotelId: Tripadvisor id for the hotel.
 * - reviewId: Tripadvisor id for the review.
 * - quote: Review's quote.
 * - review: Review's text if available.
 * - reviewerOrigin: Location of the person that wrote the review if available.
 * - address: Hotel address.
 * - hotelName: Name of the hotel.
 * - city: City where the hotel is.
 * - rank: Rank given to the hotel by the user.
 */
public class TripadvisorHotelReviewParser {

    private final static Logger LOG = LoggerFactory.getLogger(TripadvisorHotelReviewParser.class);

    private static final String quoteSelector = "#PAGEHEADING";
    private static final String hotelNameSelector = "#HR_HACKATHON_CONTENT > div.metaContent.fl > div" + "" + "" + ""
            + ".locationContent" + " > div.surContent > a";
    private static final String reviewRankSelector = "#PAGEHEADING > span.rate.sprite-rating_no.rating_no > img";
    private static final String cityNameSelector = "#BREADCRUMBS > li:nth-child(5) > a > span";
    private static final String reviewDateFormat = "yyyy-MM-dd";
    private static final String addressStreetSelector = "#HR_HACKATHON_CONTENT > div.metaContent.fl > div" + "" + ""
            + ".locationContent > div.surContent > div > address > span:nth-child(4) > span > span.street-address";
    private static final String addressExtendedSelector = "#HR_HACKATHON_CONTENT > div.metaContent.fl > div" + "" +
            "" + ".locationContent > div.surContent > div > address > span:nth-child(4) > span > span.extended-address";
    private static final String addressLocalitySelector = "#HR_HACKATHON_CONTENT > div.metaContent.fl > div" + "" +
            "" + ".locationContent > div.surContent > div > address > span:nth-child(4) > span > span.locality";
    private static final String addressCountrySelector = "#HR_HACKATHON_CONTENT > div.metaContent.fl > div" + "" + ""
            + ".locationContent > div.surContent > div > address > span:nth-child(4) > span > span.country-name";
    private static final DateTimeFormatter reviewDateFormatter = DateTimeFormatter.ofPattern(reviewDateFormat);

    private long numberOfCities = 0;
    private long numberOfHotelsInCity = 0;
    private long numberOfHotels = 0;
    private long numberOfReviews = 0;
    private long numberOfReviewsForHotel = 0;
    private long numberOfReviewsForCity = 0;
    private String currentHotelName = "";
    private String currentCityName = "";


    private TripAdvisorReview parseFile(String cityId, String hotelId, String reviewId, File reviewFile) {
        String reviewSelector = "#review_" + reviewId;
        String dateSelector = "#UR" + reviewId + " > div.col2of2 > div > div.rating.reviewItemInline > span.ratingDate";
        String reviewerLocationSelector = "#UR" + reviewId + " > div.col1of2 > div.member_info > div.location";
        String html;
        try {
            html = FileUtils.readFileToString(reviewFile, Charset.forName("UTF-8"));
            Document doc = Jsoup.parse(html);
            String quote = doc.select(this.quoteSelector).text();
            String review = "";
            String reviewerOrigin = "";
            LocalDate reviewDate = null;
            Elements reviewElement = doc.select(reviewSelector);
            Double reviewRank = Double.parseDouble(doc.select(reviewRankSelector).attr("content"));
            String hotelName = doc.select(hotelNameSelector).text();
            String cityName = doc.select(cityNameSelector).get(0).text();
            String address = extractAddress(doc);
            if (reviewElement.size() > 1) {
                review = reviewElement.get(1).text();
                reviewerOrigin = doc.select(reviewerLocationSelector).text();
                String reviewDateStr = doc.select(dateSelector).get(0).attr("content");
                reviewDate = LocalDate.parse(reviewDateStr, reviewDateFormatter);
            }
            return new TripAdvisorReview(cityId, hotelId, reviewId, quote, review, hotelName, cityName, address,
                    reviewerOrigin, reviewRank, reviewDate);
        } catch (IOException e) {
            LOG.error("Exception when extracting data from {}", reviewFile, e);
        }
        return null;
    }

    private String extractAddress(Document doc) {
        StringBuffer sb = new StringBuffer();
        String addressStreet = doc.select(addressStreetSelector).text();
        if (addressStreet != null && !addressStreet.isEmpty()) sb.append(addressStreet.trim()).append(", ");
        String addressExtended = doc.select(addressExtendedSelector).text();
        if (addressExtended != null && !addressExtended.isEmpty()) sb.append(addressExtended.trim()).append(", ");
        String addressLocality = doc.select(addressLocalitySelector).text();
        if (addressLocality != null && !addressLocality.isEmpty()) sb.append(addressLocality.trim()).append(" ");
        String addressCountry = doc.select(addressCountrySelector).text();
        if (addressCountry != null && !addressCountry.isEmpty()) sb.append(addressCountry.trim());
        return sb.toString();
    }

    public void parseAllReviews(File baseFolder) {
        LOG.info("Parsing all reviews from " + baseFolder.getPath());

        DB mongoDb = MongoDB.getProjectDB();
        DBCollection reviewCollection = mongoDb.getCollection("tripadvisor_reviews");

        List<String> allCities = Utils.listFolders(baseFolder);
        LOG.info("Found {} cities", allCities.size());
        allCities.stream().forEach(city -> {
            parseAllReviesForCity(baseFolder, reviewCollection, city);
            LOG.info("Parsed reviews {} for {} hotels in {}", numberOfReviewsForCity, numberOfHotelsInCity,
                    currentCityName);
            numberOfHotels += numberOfHotelsInCity;
            numberOfReviews += numberOfReviewsForCity;
            numberOfHotelsInCity = 0;
            numberOfReviewsForCity = 0;
        });
        LOG.info("Parsed {} reviews  for {} hotels in {} cities", numberOfReviews, numberOfHotels, numberOfCities);
    }

    private void parseAllReviesForCity(File baseFolder, DBCollection reviewCollection, String city) {
        numberOfCities++;
        File cityFolder = new File(baseFolder, city);
        List<String> allHotelsInCity = Utils.listFolders(cityFolder);
        parseAllReviewsForHotel(reviewCollection, city, cityFolder, allHotelsInCity);
    }

    private void parseAllReviewsForHotel(DBCollection reviewCollection, String city, File cityFolder, List<String>
            allHotelsInCity) {
        allHotelsInCity.stream().forEach(hotel -> {
            numberOfHotelsInCity++;
            File hotelFolder = new File(cityFolder, hotel);
            List<String> allReviewsForHotel = Utils.listFiles(hotelFolder);
            numberOfReviewsForCity += numberOfReviewsForHotel;
            allReviewsForHotel.stream().forEach(review -> {
                numberOfReviewsForHotel++;
                TripAdvisorReview tripAdvisorReview = extractDataFromReviewAndStoreInCollection(reviewCollection,
                        city, hotel, hotelFolder, review);
                currentHotelName = tripAdvisorReview == null ? "" : tripAdvisorReview.getHotelName();
                currentCityName = tripAdvisorReview == null ? "" : tripAdvisorReview.getCity();
            });
            LOG.info("Parsed {} reviews for hotel {} ({})", numberOfReviewsForHotel, currentHotelName, currentCityName);
            numberOfReviewsForHotel = 0;
        });
    }

    private TripAdvisorReview extractDataFromReviewAndStoreInCollection(DBCollection reviewCollection, String city,
                                                                        String hotel, File hotelFolder, String review) {
        TripAdvisorReview tripAdvisorReview = parseFile(city, hotel, review.split("\\.")[0], new File(hotelFolder,
                review));
        reviewCollection.insert(HotelReviewDbParser.toDocument(tripAdvisorReview));
        LOG.info(tripAdvisorReview.toString());
        return tripAdvisorReview;
    }

    public static void main(String... args) {
        if (args.length == 1) {
            TripadvisorHotelReviewParser tripAdvisorHTMLReviewParser = new TripadvisorHotelReviewParser();
            tripAdvisorHTMLReviewParser.parseAllReviews(new File(args[0]));
        } else {
            LOG.info("Enter base path for where the reviews are stored");
        }
    }

}
