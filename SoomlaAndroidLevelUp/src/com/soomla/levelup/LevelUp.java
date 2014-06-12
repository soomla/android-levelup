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

package com.soomla.levelup;

import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.scoring.Score;
import com.soomla.store.StoreUtils;
import com.soomla.store.data.StorageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The top level container for the android-levelup model and definitions.
 * It stores the configuration of all the game's worlds hierarchy and
 * provides lookup for levelup model elements.
 */
public class LevelUp {
    public static final String DB_KEY_PREFIX = "soomla.levelup.";

    public void initialize(List<World> initialWorlds) {
        HashMap<String, World> worldMap = new HashMap<String, World>();
        for (World world : initialWorlds) {
            worldMap.put(world.getWorldId(), world);
        }
        mInitialWorlds = worldMap;
        save();
    }

    /**
     * Retrieves a score object from the model
     *
     * @param scoreId the ID of the score to get
     * @return
     */
    public Score getScore(String scoreId) {
        return fetchScoreFromWorlds(scoreId, mInitialWorlds);
    }

    /**
     * Retrieves a world object from the model
     *
     * @param worldId the ID of the world to get
     * @return
     */
    public World getWorld(String worldId) {
        return fetchWorld(worldId, mInitialWorlds);
    }

    /** Singleton **/

    public static LevelUp getInstance() {
        if (sInstance == null) {
            sInstance = new LevelUp();
        }
        return sInstance;
    }

    private LevelUp() {}
    private static LevelUp sInstance;

    /**
     * Persists the entire levelup model to storage.
     */
    private void save() {
        String bp_json = toJSONObject().toString();
        StoreUtils.LogDebug(TAG, "saving LevelUp to DB. json is: " + bp_json);
        String key = DB_KEY_PREFIX + "model";
        StorageManager.getKeyValueStorage().setValue(key, bp_json);
    }

//    public void load() {}
//    public void loadFromFile(String filePath) {}
//    public void loadFromJSON(String json) {}


    /**
     * Converts the current <code>LevelUp</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>LevelUp</code>.
     */
    private JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray worldsJSON = new JSONArray();
            for (World world : mInitialWorlds.values()) {
                worldsJSON.put(world.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_WORLDS, worldsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private Score fetchScoreFromWorlds(String scoreId, HashMap<String, World> worlds) {
        Score retScore = null;
        for (World world : worlds.values()) {
            retScore = world.getScores().get(scoreId);
            if (retScore == null) {
                retScore = fetchScoreFromWorlds(scoreId, world.getInnerWorlds());
            }
            if (retScore != null) {
                break;
            }
        }

        return retScore;
    }

    private World fetchWorld(String worldId, HashMap<String, World> worlds) {
        World retWorld = worlds.get(worldId);
        if (retWorld == null) {
            for (World world : worlds.values()) {
                retWorld = fetchWorld(worldId, world.getInnerWorlds());
            }
        }

        return retWorld;
    }

    /** Private Members **/

    private static final String TAG = "SOOMLA LevelUp";
    private HashMap<String, World>   mInitialWorlds;
}
