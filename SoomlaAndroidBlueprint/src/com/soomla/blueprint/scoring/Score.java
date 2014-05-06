package com.soomla.blueprint.scoring;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class Score {
    private String mScoreId;
    protected double mScore;
    protected String mName;

    public String getScoreId() {
        return mScoreId;
    }

    public double getScore() {
        return mScore;
    }

    public String getName() {
        return mName;
    }
}
