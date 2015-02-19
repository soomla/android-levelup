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
import com.soomla.Soomla;
import com.soomla.SoomlaUtils;
import com.soomla.data.KeyValueStorage;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.events.LevelUpInitializedEvent;
import com.soomla.levelup.events.WorldAssignedRewardEvent;
import com.soomla.levelup.events.WorldCompletedEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WorldStorage {

    private static String keyWorlds(String worldId, String postfix) {
        return DB_WORLD_KEY_PREFIX + worldId + "." + postfix;
    }

    private static String keyWorldCompleted(String worldId) {
        return keyWorlds(worldId, "completed");
    }

    private static String keyReward(String worldId) {
        return keyWorlds(worldId, "assignedReward");
    }

    private static String keyLastCompletedInnerWorld(String worldId) {
        return keyWorlds(worldId, "lastCompletedInnerWorld");
    }

    public static void initLevelUp() {
        BusProvider.getInstance().post(new LevelUpInitializedEvent());
    }

    public static void setCompleted(String worldId, boolean completed) {
        setCompleted(worldId, completed, true);
    }

    public static void setCompleted(String worldId, boolean completed, boolean notify) {

        String key = keyWorldCompleted(worldId);

        if (completed) {
            KeyValueStorage.setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new WorldCompletedEvent(worldId));
            }
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }
    }

    public static boolean isCompleted(String worldId) {
        String key = keyWorldCompleted(worldId);
        String val = KeyValueStorage.getValue(key);
        return !TextUtils.isEmpty(val);
    }


    /**
     * World Reward *
     */

    public static void setReward(String worldId, String rewardId) {
        setReward(worldId, rewardId, true);
    }

    public static void setReward(String worldId, String rewardId, boolean notify) {

        String key = keyReward(worldId);
        if (!TextUtils.isEmpty(rewardId)) {
            KeyValueStorage.setValue(key, rewardId);
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }

        if (notify) {
            // Notify world was assigned a reward
            BusProvider.getInstance().post(new WorldAssignedRewardEvent(worldId));
        }
    }

    public static String getAssignedReward(String worldId) {
        String key = keyReward(worldId);
        return KeyValueStorage.getValue(key);
    }

    /**
     * Last Completed Inner World  *
     */

    public static void setLastCompletedInnerWorld(String worldId, String innerWorldId) {
        setLastCompletedInnerWorld(worldId, innerWorldId, true);
    }

    public static void setLastCompletedInnerWorld(String worldId, String innerWorldId, boolean notify) {

        String key = keyLastCompletedInnerWorld(worldId);
        if (!TextUtils.isEmpty(innerWorldId)) {
            KeyValueStorage.setValue(key, innerWorldId);
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }

        if (notify) {
            // Notify world had inner level complete
            BusProvider.getInstance().post(new WorldAssignedRewardEvent(worldId));
        }
    }

    public static String getLastCompletedInnerWorld(String worldId) {
        String key = keyLastCompletedInnerWorld(worldId);
        return KeyValueStorage.getValue(key);
    }

    public static boolean isLevel(String worldId) {
        JSONObject model = LevelUp.getLevelUpModel();
        if (model != null) {
            HashMap<String, JSONObject> worlds = LevelUp.getWorlds(model);
            JSONObject world = worlds.get(worldId);
            if (world != null) {
                try {
                    if (world.getString("itemId").equals(worldId)) {
                        return (world.getString("className").equals("Level"));
                    }
                }
                catch (JSONException ex) {
                    SoomlaUtils.LogDebug(TAG, "Model JSON is malformed " + ex.getMessage());
                }
            }
        }

        return false;
    }

    public static final String DB_WORLD_KEY_PREFIX = LevelUp.DB_KEY_PREFIX + "worlds.";
    private static final String TAG = "SOOMLA WorldStorage";
}
