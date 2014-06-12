/*
 * Copyright (C) 2012-2014 Soomla Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soomla.levelup.scoring;

import com.soomla.levelup.Level;
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.data.ScoreStorage;
import com.soomla.levelup.events.ScoreRecordChangedEvent;
import com.soomla.levelup.util.JSONFactory;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    public static final String TYPE_NAME = "score";

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
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public static Score fromJSONObject(JSONObject jsonObject) {

        return sJSONFactory.create(jsonObject, sTypeMap);

//        if(jsonObject == null) {
//            StoreUtils.LogWarning(TAG, "fromJSONObject: jsonObject is null");
//            return null;
//        }
//
//        Score score = null;
//
//        try {
//            String type = jsonObject.getString(BPJSONConsts.BP_TYPE);
//            if (type.equals(Score.TYPE_NAME)) {
//                score = new Score(jsonObject);
//            }
//            else if (type.equals(RangeScore.TYPE_NAME)) {
//                score = new RangeScore(jsonObject);
//            }
//            else if (type.equals(VirtualItemScore.TYPE_NAME)) {
//                score = new VirtualItemScore(jsonObject);
//            }
//            else {
//                StoreUtils.LogError(TAG, "unknown score type:" + type);
//            }
//        } catch (JSONException e) {
//            StoreUtils.LogError(TAG, "fromJSONObject JSONException:" + e.getMessage());
//        }
//
//        return score;
    }

    public boolean isHigherBetter() {
        return mHigherBetter;
    }

    /**
     * Increments the score of the current game session with the given amount
     *
     * @param amount the amount to increment
     */
    public void inc(double amount) {
        setTempScore(mTempScore + amount);
    }

    /**
     * Decrements the score of the current game session with the given amount
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
        double record = ScoreStorage.getRecordScore(this);
        if (hasTempReached(record)) {
            ScoreStorage.setRecordScore(this, mTempScore);
            BusProvider.getInstance().post(new ScoreRecordChangedEvent(this));
        }

        performSaveActions();

        ScoreStorage.setLatestScore(this, mTempScore);
        setTempScore(mStartValue);
    }

    /**
     * Resets the current score's value
     */
    public void reset() {
        mTempScore = mStartValue;
        ScoreStorage.setRecordScore(this, 0);
        ScoreStorage.setLatestScore(this, 0);
    }

    /**
     * Checks if the score in the current game session has reached a certain value
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
        double record = ScoreStorage.getRecordScore(this);
        return hasScoreReached(record, scoreVal);
    }

    /**
     * Score sometimes can have additional actions
     * associated with reaching/saving it.
     * Override this method to add specific score behavior
     */
    protected void performSaveActions() {}

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
        return ScoreStorage.getRecordScore(this);
    }

    public double getLatest() {
        return ScoreStorage.getLatestScore(this);
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

    private static JSONFactory<Score> sJSONFactory = new JSONFactory<Score>();
    private static Map<String, Class<? extends Score>> sTypeMap =
            new HashMap<String, Class<? extends Score>>(4);
    static {
        sTypeMap.put(Score.TYPE_NAME, Score.class);
        sTypeMap.put(RangeScore.TYPE_NAME, RangeScore.class);
        sTypeMap.put(VirtualItemScore.TYPE_NAME, VirtualItemScore.class);
    }

    protected double mStartValue;
    protected String mName;
    private String mScoreId;
    private boolean mHigherBetter;
    private double mTempScore;
}
