package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.challenges.Mission;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * Created by refaelos on 13/05/14.
 */
public class MissionsStorage {

    private static String keyMissions(String missionId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "missions." + missionId + "." + postfix;
    }

    private static String keyMissionsCompleted(String missionId) {
        return keyMissions(missionId, "completed");
    }

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

    public static boolean isCompleted(Mission mission) {
        String missionId = mission.getMissionId();
        String key = keyMissionsCompleted(missionId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }
}
