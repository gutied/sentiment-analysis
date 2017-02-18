package com.gutied.project.google;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.gutied.project.tripadvisor.TripadvisorHotelReviewReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Functionality to get the geolocation for a giving address using Google's Geocoding API.
 * <p>
 * <p>The application stores the results in the database.
 * <p>
 * <p>In order to work the GOOGLE_API_KEY environment variable needs to be set with the a valid Google API key.
 * {@link https://developers.google.com/maps/documentation/geocoding/intro}
 */
public class Geocoding {

    private static Logger LOG = LoggerFactory.getLogger(TripadvisorHotelReviewReader.class);

    public static int DELAY_BETWEEN_CALLS_IN_MILLS = 1000;

    private GeoApiContext context;

    public Geocoding() {
        this(System.getenv("GOOGLE_API_KEY"));
    }

    public Geocoding(String googleApiKey) {
        context = new GeoApiContext();
        context.setQueryRateLimit(3).setConnectTimeout(1, TimeUnit.SECONDS).setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS).setApiKey(googleApiKey);
    }

    /**
     * {@code geocodeAddress} Retruns the geolocation of a given address using Google's API
     * <p>
     * <p>If google returns multiple geolocation results this methods returns one of them.
     *
     * @param address taht we want to geolocate.
     * @return {@link GeocodingResult} Containing the geolocation result.
     */
    public GeocodingResult geocodeAddress(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context).address(address).await();
            if (results.length > 1) {
                LOG.warn("Several geocoding results received for address: {}", address);
            }
            if (results.length > 0) {
                return results == null ? null : results[0];
            }
        } catch (Exception e) {
            LOG.error("Exception when invoking Google's Geocoding API", e);
        }
        return null;
    }

}
