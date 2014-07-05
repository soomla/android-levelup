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
import com.soomla.levelup.Level;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.World;
import com.soomla.levelup.events.WorldBadgeAssignedEvent;
import com.soomla.levelup.events.WorldCompletedEvent;

/**
 * Created by refaelos on 13/05/14.
 */
public class WorldStorage {

    private static String keyWorlds(String worldId, String postfix) {
        return LevelUp.DB_KEY_PREFIX + "worlds." + worldId + "." + postfix;
    }

    private static String keyWorldCompleted(String worldId) {
        return keyWorlds(worldId, "completed");
    }

    private static String keyBadge(String worldId) {
        return keyWorlds(worldId, "badge");
    }

    public static void setCompleted(World world, boolean completed) {
        setCompleted(world, completed, true);
    }

    public static void setCompleted(World world, boolean completed, boolean notify) {
        String worldId = world.getWorldId();
        String key = keyWorldCompleted(worldId);

        if (completed) {
            KeyValueStorage.setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new WorldCompletedEvent(world));
            }
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }
    }

    public static boolean isCompleted(World world) {
        String worldId = world.getWorldId();
        String key = keyWorldCompleted(worldId);

        String val = KeyValueStorage.getValue(key);

        return val != null;
    }


    /** World Badge **/

    public static void setBadge(World world, String badgeRewardId){

        String worldId = world.getWorldId();
        String key = keyBadge(worldId);
        if (!TextUtils.isEmpty(badgeRewardId)) {
            KeyValueStorage.setValue(key, badgeRewardId);
        } else {
            KeyValueStorage.deleteKeyValue(key);
        }

        // Notify world was assigned a badge
        BusProvider.getInstance().post(new WorldBadgeAssignedEvent(world));
    }

    public static String getAssignedBadge(World world){

        String worldId = world.getWorldId();
        String key = keyBadge(worldId);

        return KeyValueStorage.getValue(key);
    }
}
