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

import com.soomla.SoomlaEntity;
import com.soomla.SoomlaUtils;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.util.JSONFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a score in the game. A simple game usually has one generic numeric score
 * which grows as the user progresses in the game. A game can also have multiple
 * <code>ScoreId</code>s for different aspects such as time, speed, points etc.
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
     * Generates an instance of <code>ScoreId</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>ScoreId</code>.
     * @throws JSONException
     */
    public Score(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mStartValue = jsonObject.getDouble(LUJSONConsts.LU_SCORE_STARTVAL);
        mHigherBetter = jsonObject.getBoolean(LUJSONConsts.LU_SCORE_HIGHBETTER);
    }

    /**
     * Converts the current <code>ScoreId</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>ScoreId</code>.
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

    public static Score fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, Score.class.getPackage().getName());
    }

    public boolean isHigherBetter() {
        return mHigherBetter;
    }


    /**
     * Setters and Getters
     */

    public double getTempScore() {
        return mTempScore;
    }

    public void setHigherBetter(boolean mHigherBetter) {
        this.mHigherBetter = mHigherBetter;
    }

    public void setStartValue(double startValue) {
        mStartValue = startValue;
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
