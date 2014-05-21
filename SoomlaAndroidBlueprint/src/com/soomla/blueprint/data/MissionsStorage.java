package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.challenges.Mission;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying the state of missions.
 * Use this class to check if a certain mission is complete, or to
 * set its completion state.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class MissionsStorage {

    private static String keyMissions(String missionId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "missions." + missionId + "." + postfix;
    }

    private static String keyMissionsCompleted(String missionId) {
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
        String key = keyMissionsCompleted(missionId);

        if (completed) {
            StorageManager.getKeyValueStorage().setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new MissionCompletedEvent(mission));
            }
        } else {
            StorageManager.getKeyValueStorage().deleteKeyValue(key);
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
        String key = keyMissionsCompleted(missionId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }
}
