package com.soomla.blueprint.scoring;

/**
 * Created by refaelos on 07/05/14.
 */
public class RangeNumberScore extends Score {
    private Range mRange;

    public RangeNumberScore(String scoreId, String name, Range range) {
        super(scoreId, name);
        this.mRange = range;
    }

    public RangeNumberScore(String scoreId, String name, boolean higherBetter, Range range) {
        super(scoreId, name, higherBetter);
        this.mRange = range;
    }

    @Override
    public void inc(double amount) {
        if (getScore() == mRange.getHigh()) {
            return;
        }
        super.inc(amount);
    }

    @Override
    public void dec(double amount) {
        if (getScore() == mRange.getLow()) {
            return;
        }
        super.dec(amount);
    }

    @Override
    public void setScore(double score) {
        if (score > mRange.getHigh()) {
            score = mRange.getHigh();
        }
        if (score < mRange.getLow()) {
            score = mRange.getLow();
        }
        super.setScore(score);
    }

    public class Range {
        private double mLow;
        private double mHigh;

        public double getLow() {
            return mLow;
        }

        public double getHigh() {
            return mHigh;
        }
    }
}
