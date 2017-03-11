package com.gutied.project.mongodb;

import com.gutied.project.tripadvisor.TripadvisorReview;
import com.mongodb.BasicDBObject;

import java.time.format.DateTimeFormatter;

public class HotelReviewDbMapper {

    public static final String tripAdvisorReviewCollection = "tripadvisor_reviews";

    private static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");

    public enum tripAdvisorReviewCollectionKeys {
        cityId, hotelId, reviewId, quote, review, reviewerOrigin, address, hotelName, city, rank, date,
        googleSentiment, googleEntities, alchemyKeywords, alchemySentiment, azureSentiment, azureEntities
    }

    public static BasicDBObject toDocument(TripadvisorReview hotelReview) {
        BasicDBObject document = new BasicDBObject();
        if (hotelReview.getDate() != null) {
            document.put(tripAdvisorReviewCollectionKeys.date.toString(), hotelReview.getDate().format(df));
        }
        document.put(tripAdvisorReviewCollectionKeys.cityId.toString(), hotelReview.getCityId());
        document.put(tripAdvisorReviewCollectionKeys.hotelId.toString(), hotelReview.getHotelId());
        document.put(tripAdvisorReviewCollectionKeys.reviewId.toString(), hotelReview.getReviewId());
        document.put(tripAdvisorReviewCollectionKeys.quote.toString(), hotelReview.getQuote());
        document.put(tripAdvisorReviewCollectionKeys.review.toString(), hotelReview.getReview());
        document.put(tripAdvisorReviewCollectionKeys.reviewerOrigin.toString(), hotelReview.getReviewerOrigin());
        document.put(tripAdvisorReviewCollectionKeys.address.toString(), hotelReview.getAddress());
        document.put(tripAdvisorReviewCollectionKeys.hotelName.toString(), hotelReview.getHotelName());
        document.put(tripAdvisorReviewCollectionKeys.city.toString(), hotelReview.getCity());
        document.put(tripAdvisorReviewCollectionKeys.rank.toString(), hotelReview.getRank());
        return document;
    }


}
