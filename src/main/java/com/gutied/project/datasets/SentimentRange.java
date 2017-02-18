package com.gutied.project.datasets;

import com.google.common.collect.Range;

public enum SentimentRange {

    negative(Range.closed(0d, 2.5d), Range.closed(-1d, 0d)), positive(Range.openClosed(2.5d, 5d), Range.openClosed
            (0d, 1d));

    private Range<Double> tripAdvisorRange;
    private Range<Double> googleRange;

    SentimentRange(Range<Double> tripAdvisorRange, Range<Double> googleRange) {
        this.tripAdvisorRange = tripAdvisorRange;
        this.googleRange = googleRange;
    }

    public static SentimentRange getTripAdvisorRange(double mark) {
        for (SentimentRange range : SentimentRange.values()) {
            if (range.getTripAdvisorRange().contains(mark)) {
                return range;
            }
        }
        return null;
    }

    public static SentimentRange getGoogleSentimentRange(double mark) {
        for (SentimentRange range : SentimentRange.values()) {
            if (range.getGoogleRange().contains(mark)) {
                return range;
            }
        }
        return null;
    }

    public Range<Double> getTripAdvisorRange() {
        return tripAdvisorRange;
    }

    public void setTripAdvisorRange(Range<Double> tripAdvisorRange) {
        this.tripAdvisorRange = tripAdvisorRange;
    }

    public Range<Double> getGoogleRange() {
        return googleRange;
    }

    public void setGoogleRange(Range<Double> googleRange) {
        this.googleRange = googleRange;
    }

}
