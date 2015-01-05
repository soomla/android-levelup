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

package com.soomla.levelup.data;

import android.text.TextUtils;

import com.soomla.BusProvider;
import com.soomla.data.KeyValueStorage;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.events.ScoreRecordChangedEvent;

/**
 * A utility class for persisting and querying scores and records.
 * Use this class to get or set the values of scores and records.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class ScoreStorage {

    private static String keyScores(String scoreId, String postfix) {
        return DB_SCORE_KEY_PREFIX + scoreId + "." + postfix;
    }

    private static String keyLatestScore(String scoreId) {
        return keyScores(scoreId, "latest");
    }

    private static String keyRecordScore(String scoreId) {
        return keyScores(scoreId, "record");
    }


    /** Latest ScoreId **/

    /**
     * Saves a new value for the given score
     *
     * @param scoreId the id of the score to change
     * @param latest the latest value to save
     */
    public static void setLatestScore(String scoreId, double latest) {
        String key = keyLatestScore(scoreId);
        String val = String.valueOf(latest);
        KeyValueStorage.setValue(key, val);
    }

    /**
     * Gets the most recently saved value of the given score.
     *
     * @param scoreId the id of the score to examine
     * @return the last saved value
     */
    public static double getLatestScore(String scoreId) {
        String key = keyLatestScore(scoreId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? -1 : Double.parseDouble(val);
    }


    /** Record ScoreId **/

    public static void setRecordScore(String scoreId, double record) {
        setRecordScore(scoreId, record, true);
    }

    /**
     * Sets a new record for the given score.
     *
     * @param scoreId the id of the score who's record to change
     * @param record the new record value
     */
    public static void setRecordScore(String scoreId, double record, boolean notify) {
        String key = keyRecordScore(scoreId);
        String val = String.valueOf(record);
        KeyValueStorage.setValue(key, val);

        if (notify) {
            BusProvider.getInstance().post(new ScoreRecordChangedEvent(scoreId));
        }
    }

    /**
     * Retrieves the record of the given score
     *
     * @param scoreId the id of the score to examine
     * @return the record of the given score
     */
    public static double getRecordScore(String scoreId) {
        String key = keyRecordScore(scoreId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? -1 : Double.parseDouble(val);
    }

    public static final String DB_SCORE_KEY_PREFIX = LevelUp.DB_KEY_PREFIX + "scores.";
}
