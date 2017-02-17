package com.gutied.project.opinrank;

import java.util.Date;

public class OpinRankHotelReview {
    private String source;
    private String city;
    private String country;
    private String hotelName;
    private String review;
    private Date date;

    public OpinRankHotelReview(Date date, String citi, String country, String hotelName, String review) {
        this.date = date;
        this.city = citi;
        this.country = country;
        this.hotelName = hotelName;
        this.review = review;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;

    }
}