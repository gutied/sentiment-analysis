package com.gutied.project.opinrank;

import com.mongodb.BasicDBObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class OpinRankHotelReviewMapper {

    private static DateFormat df = new SimpleDateFormat("yyyyMMdd");

    public static BasicDBObject toDocument (OpinRankHotelReview hotelReview) {
        BasicDBObject document = new BasicDBObject();
        if (hotelReview.getDate() != null) {
            document.put("date",  df.format(hotelReview.getDate()));
        }
        document.put("city", hotelReview.getCity());
        document.put("country", hotelReview.getCountry());
        document.put("hotel", hotelReview.getHotelName());
        document.put("review", hotelReview.getReview());
        return document;
    }

}
