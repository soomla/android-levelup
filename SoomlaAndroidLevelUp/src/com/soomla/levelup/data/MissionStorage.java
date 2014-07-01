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

import com.soomla.SoomlaApp;
import com.soomla.SoomlaUtils;
import com.soomla.data.KeyValDatabase;
import com.soomla.data.KeyValueStorage;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.events.MissionCompletedEvent;
import com.soomla.BusProvider;
import com.soomla.levelup.events.MissionCompletionRevokedEvent;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying the state of missions.
 * Use this class to check if a certain mission is complete, or to
 * set its completion state.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class MissionStorage {

    private static String keyMissions(String missionId, String postfix) {
        return LevelUp.DB_KEY_PREFIX + "missions." + missionId + "." + postfix;
    }

    private static String keyMissionCompleted(String missionId) {
        return keyMissions(missionId, "completed");
    }

    /**
     * Sets the completion status of the given mission.
     *
     * @param mission the mission to complete
     * @param completed the completed status
     */
    public static void setCompleted(Mission mission, boolean completed) {
        setCompleted(mission, completed, true);
    }

    public static void setCompleted(Mission mission, boolean completed, boolean notify) {
        String missionId = mission.getMissionId();
        String key = keyMissionCompleted(missionId);

        if (completed) {
            KeyValueStorage.setValue(key, "yes");
            if (notify) {
                BusProvider.getInstance().post(new MissionCompletedEvent(mission));
            }
        } else {
            KeyValueStorage.deleteKeyValue(key);
            if (notify) {
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
        String missionId = mission.getMissionId();
        String key = keyMissionCompleted(missionId);

        String val = KeyValueStorage.getValue(key);

        return val != null;
    }
}
