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
import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.events.MissionCompletedEvent;
import com.soomla.levelup.events.MissionCompletionRevokedEvent;

/**
 * A utility class for persisting and querying the state of missions.
 * Use this class to check if a certain mission is complete, or to
 * set its completion state.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class MissionStorage {

    private static String keyMissions(String missionId, String postfix) {
        return LevelUp.DB_KEY_PREFIX + "missions." + missionId + "." + postfix;
    }

    private static String keyMissionTimesCompleted(String missionId) {
        return keyMissions(missionId, "timesCompleted");
    }

    /**
     * Sets the completion status of the given mission.
     *
     * @param mission   the mission to complete
     * @param completed the completed status
     */
    public static void setCompleted(Mission mission, boolean completed) {
        setCompleted(mission, completed, true);
    }

    public static void setCompleted(Mission mission, boolean completed, boolean notify) {
        int total = getTimesCompleted(mission) + (completed ? 1 : -1);
        if (total < 0) {
            total = 0;
        }

        String key = keyMissionTimesCompleted(mission.getID());
        KeyValueStorage.setValue(key, String.valueOf(total));

        if (notify) {
            if (completed) {
                BusProvider.getInstance().post(new MissionCompletedEvent(mission));
            } else {
                BusProvider.getInstance().post(new MissionCompletionRevokedEvent(mission));
            }
        }
    }

    /**
     * Checks whether the given mission is complete.
     *
     * @param mission the mission to check
     * @return <code>true</code> if the mission's status is complete,
     * <code>false</code> otherwise
     */
    public static boolean isCompleted(Mission mission) {
        return getTimesCompleted(mission) > 0;
    }

    /**
     * Fetches the number of times the mission has been completed.
     *
     * @param mission the mission to check
     * @return the number of times the mission has been completed, 0 by default.
     */
    public static int getTimesCompleted(Mission mission) {
        String key = keyMissionTimesCompleted(mission.getID());
        String val = KeyValueStorage.getValue(key);
        if (TextUtils.isEmpty(val)) {
            return 0;
        }
        return Integer.parseInt(val);
    }

}
