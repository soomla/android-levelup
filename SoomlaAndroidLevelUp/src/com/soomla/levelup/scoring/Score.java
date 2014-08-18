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

import com.soomla.BusProvider;
import com.soomla.SoomlaEntity;
import com.soomla.SoomlaUtils;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.data.ScoreStorage;
import com.soomla.levelup.events.ScoreRecordReachedEvent;
import com.soomla.util.JSONFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a score in the game. A simple game usually has one generic numeric score
 * which grows as the user progresses in the game. A game can also have multiple
 * <code>Score</code>s for different aspects such as time, speed, points etc.
 * A score can be ascending in nature such as regular points (higher is better) or can
 * be descending such as time-to-complete level (lower is better).
 * <p/>
 * Created by refaelos on 06/05/14.
 */
public class Score extends SoomlaEntity<Score> {

    /**
     * Constructor
     *
     * @param id the score's ID
     */
    public Score(String id) {
        this(id, "", true);
    }

    /**
     * Constructor
     *
     * @param id           the score's ID
     * @param name         the score's name (something you might want to display on the screen)
     * @param higherBetter an indicator for an ascending or descending score scale
     */
    public Score(String id, String name, boolean higherBetter) {
        super(name, "", id);
        mHigherBetter = higherBetter;
    }

    /**
     * Constructor.
     * Generates an instance of <code>Score</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Score</code>.
     * @throws JSONException
     */
    public Score(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mStartValue = jsonObject.getDouble(LUJSONConsts.LU_SCORE_STARTVAL);
        mHigherBetter = jsonObject.getBoolean(LUJSONConsts.LU_SCORE_HIGHBETTER);
    }

    /**
     * Converts the current <code>Score</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Score</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_SCORE_STARTVAL, mStartValue);
            jsonObject.put(LUJSONConsts.LU_SCORE_HIGHBETTER, mHigherBetter);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * For JNI purposes
     *
     * @param jsonString
     * @return a score from a JSON string
     */
    public static Score fromJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJSONObject(jsonObject);
        } catch (JSONException e) {
            return null;
        }
    }

    public static Score fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, Score.class.getPackage().getName());
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
     * Resets the score to its initial value. Use this method for example
     * when a user restarts a level with a fresh score of 0.
     */
    public void reset(boolean save) {
        if (save) {
            double record = ScoreStorage.getRecordScore(this);
            if (hasTempReached(record)) {
                ScoreStorage.setRecordScore(this, mTempScore);
                mScoreRecordReachedSent = false;
            }

            performSaveActions();

            ScoreStorage.setLatestScore(this, mTempScore);
        }

        setTempScore(mStartValue);
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
    protected void performSaveActions() {
    }

    private boolean hasScoreReached(double score1, double score2) {
        return this.isHigherBetter() ?
                (score1 >= score2) :
                (score1 <= score2);
    }


    /**
     * Setters and Getters
     */

    public double getTempScore() {
        return mTempScore;
    }

    public double getRecord() {
        return ScoreStorage.getRecordScore(this);
    }

    public double getLatest() {
        return ScoreStorage.getLatestScore(this);
    }

    public void setHigherBetter(boolean mHigherBetter) {
        this.mHigherBetter = mHigherBetter;
    }

    public void setStartValue(double startValue) {
        mStartValue = startValue;
    }

    public void setTempScore(double tempScore) {
        setTempScore(tempScore, false);
    }

    public void setTempScore(double tempScore, boolean onlyIfBetter) {
        if (onlyIfBetter && !hasScoreReached(tempScore, mTempScore)) {
            return;
        }
        if (!mScoreRecordReachedSent && hasScoreReached(tempScore, tempScore)) {
            BusProvider.getInstance().post(new ScoreRecordReachedEvent(this));
            mScoreRecordReachedSent = true;
        }

        this.mTempScore = tempScore;
    }

    public double getStartValue() {
        return mStartValue;
    }

    /**
     * Private Members *
     */

    private static String TAG = "SOOMLA Score";

    private static JSONFactory<Score> sJSONFactory = new JSONFactory<Score>();

    protected double mStartValue = 0;
    private boolean mHigherBetter;
    private double mTempScore;
    private boolean mScoreRecordReachedSent;
}
