package com.gutied.project.datasets;

public enum OpinionRange {

    negative(0, 2.5), positive(2.5, 5);

    private double min;
    private double max;

    OpinionRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static OpinionRange getRange(double mark) {
        for (OpinionRange range : OpinionRange.values()) {
            if (mark > range.min && mark <= range.max) {
                return range;
            }
        }
        return null;
    }

}
