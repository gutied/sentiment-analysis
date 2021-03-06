package com.gutied.project.datasets;

import com.google.common.collect.Range;

public enum SentimentRange {

    negative(Range.closed(0d, 2.5d), Range.closed(-1d, 0d), Range.closed(0d, 0.5d), Range.closed(-1d, 0d)),
    positive(Range.openClosed(2.5d, 5d), Range.openClosed(0d, 1d), Range.openClosed(0.5d, 1d), Range.openClosed(0d, 1d));

    private Range<Double> tripAdvisorRange;
    private Range<Double> googleRange;
    private Range<Double> azureRange;
    private Range<Double> alchemyRange;

    SentimentRange(Range<Double> tripAdvisorRange, Range<Double> googleRange, Range<Double> azureRange, Range<Double> alchemyRange) {
        this.tripAdvisorRange = tripAdvisorRange;
        this.googleRange = googleRange;
        this.azureRange = azureRange;
        this.alchemyRange = alchemyRange;
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

    public static SentimentRange getAzureSentimentRange(double mark) {
        for (SentimentRange range : SentimentRange.values()) {
            if (range.getAzureRange().contains(mark)) {
                return range;
            }
        }
        return null;
    }

    public static SentimentRange getAlchemySentimentRange(double mark) {
        for (SentimentRange range : SentimentRange.values()) {
            if (range.getAlchemyRange().contains(mark)) {
                return range;
            }
        }
        return null;
    }

    public Range<Double> getTripAdvisorRange() {
        return tripAdvisorRange;
    }

    public Range<Double> getGoogleRange() {
        return googleRange;
    }

    public Range<Double> getAzureRange() {
        return azureRange;
    }

    public Range<Double> getAlchemyRange() {
        return alchemyRange;
    }

}
