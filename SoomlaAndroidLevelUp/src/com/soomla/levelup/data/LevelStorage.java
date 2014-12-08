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
import com.soomla.levelup.events.LevelEndedEvent;
import com.soomla.levelup.events.LevelStartedEvent;

/**
 * A utility class for persisting and querying the state of levels.
 * Use this class to check if a certain gate is open, or to open it.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class LevelStorage {

    private static String keyLevels(String levelId, String postfix) {
        return LevelUp.DB_KEY_PREFIX + "levels." + levelId + "." + postfix;
    }

    private static String keyTimesStarted(String levelId) {
        return keyLevels(levelId, "started");
    }

    private static String keyTimesPlayed(String levelId) {
        return keyLevels(levelId, "played");
    }

    private static String keyTimesCompleted(String worldId) {
        return keyLevels(worldId, "timesCompleted");
    }

    private static String keySlowestDuration(String levelId) {
        return keyLevels(levelId, "slowest");
    }

    private static String keyFastestDuration(String levelId) {
        return keyLevels(levelId, "fastest");
    }


    /**
     * Level Duration *
     */

    public static void setSlowestDurationMillis(String levelId, long duration) {
        String key = keySlowestDuration(levelId);
        String val = String.valueOf(duration);
        KeyValueStorage.setValue(key, val);
    }

    public static long getSlowestDurationMillis(String levelId) {
        String key = keySlowestDuration(levelId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? 0 : Long.parseLong(val);
    }

    public static void setFastestDurationMillis(String levelId, long duration) {
        String key = keyFastestDuration(levelId);
        String val = String.valueOf(duration);
        KeyValueStorage.setValue(key, val);
    }

    public static long getFastestDurationMillis(String levelId) {
        String key = keyFastestDuration(levelId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? 0 : Long.parseLong(val);
    }


    /**
     * Level Times Started *
     */

    public static int incTimesStarted(String levelId) {
        int started = getTimesStarted(levelId);
        if (started < 0) { /* can't be negative */
            started = 0;
        }
        String startedStr = "" + (started + 1);
        String key = keyTimesStarted(levelId);
        KeyValueStorage.setValue(key, startedStr);

        // Notify level has started
        BusProvider.getInstance().post(new LevelStartedEvent(levelId));

        return started + 1;
    }

    public static int decTimesStarted(String levelId) {
        int started = getTimesStarted(levelId);
        if (started <= 0) { /* can't be negative or zero */
            return 0;
        }
        String startedStr = "" + (started - 1);
        String key = keyTimesStarted(levelId);
        KeyValueStorage.setValue(key, startedStr);

        return started - 1;
    }

    public static int getTimesStarted(String levelId) {
        String key = keyTimesStarted(levelId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? 0 : Integer.parseInt(val);
    }


    /**
     * Level Times Played *
     */

    public static int incTimesPlayed(String levelId) {
        int played = getTimesPlayed(levelId);
        if (played < 0) { /* can't be negative */
            played = 0;
        }
        String playedStr = "" + (played + 1);
        String key = keyTimesPlayed(levelId);
        KeyValueStorage.setValue(key, playedStr);

        // Notify level has ended
        BusProvider.getInstance().post(new LevelEndedEvent(levelId));

        return played + 1;
    }

    public static int decTimesPlayed(String levelId) {
        int played = getTimesPlayed(levelId);
        if (played <= 0) { /* can't be negative or zero */
            return 0;
        }
        String playedStr = "" + (played - 1);
        String key = keyTimesPlayed(levelId);
        KeyValueStorage.setValue(key, playedStr);

        return played - 1;
    }

    public static int getTimesPlayed(String levelId) {
        String key = keyTimesPlayed(levelId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? 0 : Integer.parseInt(val);
    }

    /**
     * Level Times Completed *
     */

    public static int incTimesCompleted(String levelId) {
        int completed = getTimesCompleted(levelId);
        if (completed < 0) { /* can't be negative */
            completed = 0;
        }
        String completedStr = "" + (completed + 1);
        String key = keyTimesCompleted(levelId);
        KeyValueStorage.setValue(key, completedStr);

        return completed + 1;
    }

    public static int decTimesCompleted(String levelId) {
        int completed = getTimesCompleted(levelId);
        if (completed <= 0) { /* can't be negative or zero */
            return 0;
        }
        String completedStr = "" + (completed - 1);
        String key = keyTimesCompleted(levelId);
        KeyValueStorage.setValue(key, completedStr);

        return completed - 1;
    }

    public static int getTimesCompleted(String levelId) {
        String key = keyTimesCompleted(levelId);
        String val = KeyValueStorage.getValue(key);
        return TextUtils.isEmpty(val) ? 0 : Integer.parseInt(val);
    }

}
