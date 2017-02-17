package com.gutied.project.google;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GeocodingDbParserTest {

    private static final String GOOGLE_API_KEY = "AIzaSyCSDJmh9cE67OP-mbgjyx-cXDIYTYB7yzU";
    private static final String ULL_ADDRESS = "Calle Heraclio Sánchez, 43, 38204 San Cristóbal de La Laguna, Santa Cruz de Tenerife, Spain";

    @Test
    public void geocodeAddress() {
        Geocoding geocoding = new Geocoding(GOOGLE_API_KEY);
        assertNotNull(geocoding.geocodeAddress(ULL_ADDRESS));
    }

}