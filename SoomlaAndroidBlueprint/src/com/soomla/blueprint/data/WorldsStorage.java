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

package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.World;
import com.soomla.blueprint.events.WorldCompletedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * Created by refaelos on 13/05/14.
 */
public class WorldsStorage {

    private static String keyWorlds(String worldId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "worlds." + worldId + "." + postfix;
    }

    private static String keyWorldsCompleted(String worldId) {
        return keyWorlds(worldId, "completed");
    }

    public static void setCompleted(World world, boolean completed) {
        setCompleted(world, completed, true);
    }

    public static void setCompleted(World world, boolean completed, boolean notify) {
        String worldId = world.getWorldId();
        String key = keyWorldsCompleted(worldId);

        if (completed) {
            StorageManager.getKeyValueStorage().setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new WorldCompletedEvent(world));
            }
        } else {
            StorageManager.getKeyValueStorage().deleteKeyValue(key);
        }
    }

    public static boolean isCompleted(World world) {
        String worldId = world.getWorldId();
        String key = keyWorldsCompleted(worldId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }
}
