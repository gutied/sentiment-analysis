package com.gutied.project.tripadvisor;


import java.time.LocalDate;

public class TripAdvisorReview {

    private String cityId;
    private String hotelId;
    private String reviewId;
    private String quote;
    private String review;
    private String hotelName;
    private String city;
    private String address;
    private String reviewerOrigin;
    private Double rank;
    private LocalDate date;

    public TripAdvisorReview(String cityId, String hotelId, String reviewId, String quote, String review, String hotelName, String city, String address, String reviewerOrigin, Double rank, LocalDate date) {
        this.cityId = cityId;
        this.hotelId = hotelId;
        this.reviewId = reviewId;
        this.quote = quote;
        this.review = review;
        this.hotelName = hotelName;
        this.city = city;
        this.address = address;
        this.reviewerOrigin = reviewerOrigin;
        this.rank = rank;
        this.date = date;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getRank() {
        return rank;
    }

    public void setRank(Double rank) {
        this.rank = rank;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReviewerOrigin() {
        return reviewerOrigin;
    }

    public void setReviewerOrigin(String reviewerOrigin) {
        this.reviewerOrigin = reviewerOrigin;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TripAdvisorReview{" +
                "cityId='" + cityId + '\'' +
                ", hotelId='" + hotelId + '\'' +
                ", reviewId='" + reviewId + '\'' +
                ", city='" + city + '\'' +
                ", hotelName='" + hotelName + '\'' +
                ", date=" + date +
                ", rank=" + rank +
                ", quote='" + quote + '\'' +
                ", address='" + address + '\'' +
                ", reviewerOrigin='" + reviewerOrigin + '\'' +
                '}';
    }
}
