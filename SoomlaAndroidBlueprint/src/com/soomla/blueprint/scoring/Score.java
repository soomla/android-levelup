package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.ScoresStorage;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.store.BusProvider;

/**
 * Created by refaelos on 06/05/14.
 */
public class Score {
    private String mScoreId;
    private double mTempScore;
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
    public double getTempScore() {
        return mTempScore;
    }
    public double getRecord() {
        return ScoresStorage.getRecordScore(this);
    }
    public double getLatest() {
        return ScoresStorage.getLatestScore(this);
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

    public void setTempScore(double tempScore) {
        this.mTempScore = tempScore;
    }
    public void inc(double amount) {
        setTempScore(mTempScore + amount);
    }
    public void dec(double amount) {
        setTempScore(mTempScore - amount);
    }
    public void saveAndReset() {
        double record = ScoresStorage.getRecordScore(this);
        if (hasTempReached(record)) {
            ScoresStorage.setRecordScore(this, mTempScore);
            BusProvider.getInstance().post(new ScoreRecordChangedEvent(this));
        }
        ScoresStorage.setLatestScore(this, mTempScore);
        setTempScore(mStartValue);
    }
    public void reset() {
        mTempScore = mStartValue;
        ScoresStorage.setRecordScore(this, 0);
        ScoresStorage.setLatestScore(this, 0);
    }

    public boolean hasTempReached(double scoreVal) {
        return hasScoreReached(mTempScore, scoreVal);
    }

    public boolean hasRecordReached(double scoreVal) {
        double record = ScoresStorage.getRecordScore(this);
        return hasScoreReached(record, scoreVal);
    }

    private boolean hasScoreReached(double score1, double score2) {
        return this.isHigherBetter() ?
                (score1 >= score2) :
                (score1 <= score2);
    }
}
