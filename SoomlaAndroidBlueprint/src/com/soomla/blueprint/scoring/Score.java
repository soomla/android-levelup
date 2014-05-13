package com.soomla.blueprint.scoring;

import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.store.BusProvider;

/**
 * Created by refaelos on 06/05/14.
 */
public class Score {
    private String mScoreId;
    private double mScore;
    private double mRecord;
    private double mLatest;
    protected double mStartValue;
    protected String mName;
    private boolean mHigherBetter;

    public Score(String scoreId, String name) {
        this.mScoreId = scoreId;
        this.mName = name;
        this.mStartValue = 0;
        this.mHigherBetter = true;
    }

    public Score(String scoreId, String name, boolean higherBetter) {
        this.mScoreId = scoreId;
        this.mName = name;
        this.mStartValue = 0;
        this.mHigherBetter = higherBetter;
    }

    public String getScoreId() {
        return mScoreId;
    }
    public double getScore() {
        return mScore;
    }
    public double getRecord() {
        return mRecord;
    }
    public double getLatest() {
        return mLatest;
    }

    public String getName() {
        return mName;
    }

    public boolean isHigherBetter() {
        return mHigherBetter;
    }
    public void setHigherBetter(boolean mHigherBetter) {
        this.mHigherBetter = mHigherBetter;
    }

    public void setStartValue(double startValue) {
        mStartValue = startValue;
    }

    public void setScore(double score) {
        this.mScore = score;
        if (hasCurrentReached(mRecord)) {
            mRecord = mScore;
            BusProvider.getInstance().post(new ScoreRecordChangedEvent(this));
        }
    }
    public void inc(double amount) {
        setScore(mScore+amount);
    }
    public void dec(double amount) {
        setScore(mScore-amount);
    }
    public void reset() {
        mLatest = mScore;
        setScore(mStartValue);
    }

    public boolean hasCurrentReached(double scoreVal) {
        return hasScoreReached(mScore, scoreVal);
    }

    public boolean hasRecordReached(double scoreVal) {
        return hasScoreReached(mRecord, scoreVal);
    }

    private boolean hasScoreReached(double score1, double score2) {
        return this.isHigherBetter() ?
                (score1 >= score2) :
                (score1 <= score2);
    }
}
