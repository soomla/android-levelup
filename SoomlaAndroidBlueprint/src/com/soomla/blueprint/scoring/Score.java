package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.ScoresStorage;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 06/05/14.
 */
public class Score {
    private static String TAG = "SOOMLA Score";

    private String mScoreId;
    protected double mStartValue;
    protected String mName;
    private boolean mHigherBetter;

    private double mTempScore;

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

    public Score(JSONObject jsonObject) throws JSONException{
        mScoreId = jsonObject.getString(BPJSONConsts.BP_SCORE_SCOREID);
        try{
            mName = jsonObject.getString(BPJSONConsts.BP_NAME);
        } catch (JSONException ignored) {}
        mStartValue = jsonObject.getDouble(BPJSONConsts.BP_SCORE_STARTVAL);
        mHigherBetter = jsonObject.getBoolean(BPJSONConsts.BP_SCORE_HIGHBETTER);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_SCORE_SCOREID, mScoreId);
            jsonObject.put(BPJSONConsts.BP_NAME, mName);
            jsonObject.put(BPJSONConsts.BP_SCORE_STARTVAL, mStartValue);
            jsonObject.put(BPJSONConsts.BP_SCORE_HIGHBETTER, mHigherBetter);
            jsonObject.put(BPJSONConsts.BP_TYPE, "score");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
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
