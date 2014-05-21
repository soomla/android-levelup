package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.ScoresStorage;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a score in the game. A simple game usually has one generic numeric score
 * which grows as the user progresses in the game. A game can also have multiple
 * <code>Score</code>s for different aspects such as time, speed, points etc.
 * A score can be ascending in nature such as regular points (higher is better) or can
 * be descending such as time-to-complete level (lower is better).
 *
 * Created by refaelos on 06/05/14.
 */
public class Score {

    /**
     * Constructor
     *
     * @param scoreId the score's ID
     * @param name the score's name (something you might want to display on the screen)
     */
    public Score(String scoreId, String name) {
        this.mScoreId = scoreId;
        this.mName = name;
        this.mStartValue = 0;
        this.mHigherBetter = true;
    }

    /**
     * Constructor
     *
     * @param scoreId the score's ID
     * @param name the score's name (something you might want to display on the screen)
     * @param higherBetter an indicator for an ascending or descending score scale
     */
    public Score(String scoreId, String name, boolean higherBetter) {
        this.mScoreId = scoreId;
        this.mName = name;
        this.mStartValue = 0;
        this.mHigherBetter = higherBetter;
    }

    /**
     * Constructor.
     * Generates an instance of <code>Score</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Score</code>.
     * @throws JSONException
     */
    public Score(JSONObject jsonObject) throws JSONException{
        mScoreId = jsonObject.getString(BPJSONConsts.BP_SCORE_SCOREID);
        try{
            mName = jsonObject.getString(BPJSONConsts.BP_NAME);
        } catch (JSONException ignored) {}
        mStartValue = jsonObject.getDouble(BPJSONConsts.BP_SCORE_STARTVAL);
        mHigherBetter = jsonObject.getBoolean(BPJSONConsts.BP_SCORE_HIGHBETTER);
    }

    /**
     * Converts the current <code>Score</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Score</code>.
     */
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

    public boolean isHigherBetter() {
        return mHigherBetter;
    }

    /**
     * Increments the score with the given amount
     *
     * @param amount the amount to increment
     */
    public void inc(double amount) {
        setTempScore(mTempScore + amount);
    }

    /**
     * Decrements the score with the given amount
     *
     * @param amount the amount to decrement
     */
    public void dec(double amount) {
        setTempScore(mTempScore - amount);
    }

    /**
     * Saves the current score (and record if reached) and resets
     * the score to its initial value.  Use this method for example
     * when a user restarts a level with a fresh score of 0.
     */
    public void saveAndReset() {
        double record = ScoresStorage.getRecordScore(this);
        if (hasTempReached(record)) {
            ScoresStorage.setRecordScore(this, mTempScore);
            BusProvider.getInstance().post(new ScoreRecordChangedEvent(this));
        }
        ScoresStorage.setLatestScore(this, mTempScore);
        setTempScore(mStartValue);
    }

    /**
     * Resets the current score's value
     */
    public void reset() {
        mTempScore = mStartValue;
        ScoresStorage.setRecordScore(this, 0);
        ScoresStorage.setLatestScore(this, 0);
    }

    /**
     * Checks if the current score has reached a certain value
     *
     * @param scoreVal the value to check
     * @return <code>true</code> if the score has reached the desired value,
     * <code>false</code> otherwise
     */
    public boolean hasTempReached(double scoreVal) {
        return hasScoreReached(mTempScore, scoreVal);
    }

    /**
     * Checks if the score has reached a record
     *
     * @param scoreVal the value of the record
     * @return <code>true</code> if the score has reached the record,
     * <code>false</code> otherwise
     */
    public boolean hasRecordReached(double scoreVal) {
        double record = ScoresStorage.getRecordScore(this);
        return hasScoreReached(record, scoreVal);
    }

    private boolean hasScoreReached(double score1, double score2) {
        return this.isHigherBetter() ?
                (score1 >= score2) :
                (score1 <= score2);
    }


    /** Setters and Getters */

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

    public void setHigherBetter(boolean mHigherBetter) {
        this.mHigherBetter = mHigherBetter;
    }


    public void setStartValue(double startValue) {
        mStartValue = startValue;
    }

    public void setTempScore(double tempScore) {
        this.mTempScore = tempScore;
    }


    /** Private Members **/

    private static String TAG = "SOOMLA Score";

    protected double mStartValue;
    protected String mName;
    private String mScoreId;
    private boolean mHigherBetter;
    private double mTempScore;
}
