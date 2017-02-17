package com.gutied.project.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class GoogleGeocodingApiDbParser {

    public static final String geolocationCollection = "geocoding";

    public enum geolocationCollectionKeys { hotelId, hotelName, address, lat, lng}

    public static BasicDBObject createHotelGeolocationObject (DBObject hotelReview, double lat, double lng) {
        BasicDBObject document = new BasicDBObject();
        document.put(geolocationCollectionKeys.hotelId.toString(), hotelReview.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelId.toString()));
        document.put(geolocationCollectionKeys.hotelName.toString(), hotelReview.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.hotelName.toString()));
        document.put(geolocationCollectionKeys.address.toString(), hotelReview.get(HotelReviewDbParser.tripAdvisorReviewCollectionKeys.address.toString()));
        document.put(geolocationCollectionKeys.lat.toString(), lat);
        document.put(geolocationCollectionKeys.lng.toString(), lng);
        return document;
    }

}
