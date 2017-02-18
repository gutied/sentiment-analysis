package com.gutied.project.tasks;

import com.google.maps.model.GeocodingResult;
import com.gutied.project.google.Geocoding;
import com.gutied.project.mongodb.GoogleGeocodingDbMapper;
import com.gutied.project.mongodb.HotelReviewDbMapper;
import com.gutied.project.mongodb.MongoDB;
import com.gutied.project.tripadvisor.TripadvisorHotelReviewReader;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GeolocateHotels {

    private static Logger LOG = LoggerFactory.getLogger(TripadvisorHotelReviewReader.class);

    public void geolocateAllHotelsAddressesAndSave() {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection reviewCollection = mongoDb.getCollection(HotelReviewDbMapper.tripAdvisorReviewCollection);
        DBCollection geolocationCollection = mongoDb.getCollection(GoogleGeocodingDbMapper.geolocationCollection);
        Geocoding geocoding = new Geocoding();
        List<String> hotelNames = reviewCollection.distinct(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys
                .hotelName.toString());
        LOG.info("There are {} hotels", hotelNames.size());

        hotelNames.forEach(name -> {
            DBObject reviewWithHotelAddress = retrieveFirstReviewForAddress(reviewCollection, name);
            String address = (String) reviewWithHotelAddress.get(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys
                    .address.toString());
            if (address != null && !address.isEmpty()) {
                GeocodingResult geocodingResult = geocoding.geocodeAddress(address);
                if (geocodingResult != null) {
                    DBObject geolocationDbObject = GoogleGeocodingDbMapper.createHotelGeolocationObject
                            (reviewWithHotelAddress, geocodingResult.geometry.location.lat, geocodingResult.geometry
                                    .location.lng);
                    LOG.info("New geolocation object: {}", geolocationDbObject);
                    geolocationCollection.insert(geolocationDbObject);
                }
                waitForNextRequestToGeocodingApi();
            }
        });

    }

    private void waitForNextRequestToGeocodingApi() {
        try {
            Thread.sleep(Geocoding.DELAY_BETWEEN_CALLS_IN_MILLS);
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }

    private DBObject retrieveFirstReviewForAddress(DBCollection reviewCollection, String name) {
        BasicDBObject query = new BasicDBObject();
        query.put(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.hotelName.toString(), name);
        BasicDBObject fields = new BasicDBObject();
        fields.put(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.hotelId.toString(), 1);
        fields.put(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.address.toString(), 1);
        fields.put(HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.hotelName.toString(), 1);
        DBCursor cursor = reviewCollection.find(query, fields);
        return cursor.next();
    }

    public static void main(String... args) {
        GeolocateHotels geolocateHotels = new GeolocateHotels();
        geolocateHotels.geolocateAllHotelsAddressesAndSave();
    }

}
