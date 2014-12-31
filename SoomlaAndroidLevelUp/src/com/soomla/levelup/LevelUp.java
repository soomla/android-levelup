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

import com.soomla.Soomla;
import com.soomla.SoomlaUtils;
import com.soomla.data.KeyValueStorage;
import com.soomla.levelup.data.GateStorage;
import com.soomla.levelup.data.LevelStorage;
import com.soomla.levelup.data.MissionStorage;
import com.soomla.levelup.data.ScoreStorage;
import com.soomla.levelup.data.WorldStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * The top level container for the android-levelup model and definitions.
 * It stores the configuration of all the game's worlds hierarchy and
 * provides lookup for levelup model elements.
 */
public class LevelUp {

    public static final String DB_KEY_PREFIX = "soomla.levelup.";

    public static JSONObject getLevelUpState() {
        JSONObject stateJSON = new JSONObject();

        JSONObject modelJSON = getLevelUpModel();
        if (modelJSON == null) {
            return stateJSON;
        }

        applyGatesStateToJSON(modelJSON, stateJSON);
        applyWorldsStateToJSON(modelJSON, stateJSON);
        applyMissionsStateToJSON(modelJSON, stateJSON);
        applyScoresStateToJSON(modelJSON, stateJSON);

        return stateJSON;
    }

    public static boolean resetLevelUpState(JSONObject state) {
        if (state == null) {
            return false;
        }

        SoomlaUtils.LogDebug(TAG, "Resetting state with: " + state.toString());

        clearCurrentState();

        SoomlaUtils.LogDebug(TAG, "Current state was cleared");

        return resetGatesStateFromJSON(state) &&
                resetWorldsStateFromJSON(state) &&
                resetMissionsStateFromJSON(state) &&
                resetScoresStateFromJSON(state);
    }

    public static JSONObject getLevelUpModel() {
        JSONObject modelJSON = null;

        String model = KeyValueStorage.getValue(DB_KEY_PREFIX + "model");
        SoomlaUtils.LogDebug(TAG, "model: " + model);
        if (model == null) {
            return null;
        }

        try {
            modelJSON = new JSONObject(model);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "Unable to parse LevelUp model into JSON");
        }

        return modelJSON;
    }

    public static HashMap<String, JSONObject> getWorlds(JSONObject model) {
        HashMap<String, JSONObject> worlds = new HashMap<String, JSONObject>();

        try {
            JSONObject mainWorld = model.getJSONObject("mainWorld");
            addWorldObjectToWorlds(worlds, mainWorld);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "couldn't get something from worldJSON. error: " + e.getLocalizedMessage());
        }

        return worlds;
    }

    public static HashMap<String, JSONObject> getMissions(JSONObject model) {
        HashMap<String, JSONObject> missions = getListFromWorlds(model, "missions");
        findInternalLists(missions, new String[]{ "Challenge" }, "missions");

        return missions;
    }

    public static HashMap<String, JSONObject> getGates(JSONObject model) {
        HashMap<String, JSONObject> resultHash = new HashMap<String, JSONObject>();

        try {
            HashMap<String, JSONObject> worldJSONs = getWorlds(model);

            for (JSONObject worldJSON : worldJSONs.values()) {
                JSONObject gateJSON = worldJSON.getJSONObject("gate");

                String objectId = gateJSON.getString("itemId");
                resultHash.put(objectId, gateJSON);
            }
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "couldn't get gates from worldJSON. error: " + e.getLocalizedMessage());
        }

        findInternalLists(resultHash, new String[] {"GatesListAND", "GatesListOR"}, "gates");

        return resultHash;
    }

    public static HashMap<String, JSONObject> getScores(JSONObject model) {
        return getListFromWorlds(model, "scores");
    }

    private static void clearCurrentState() {
        List<String> allKeys = KeyValueStorage.getEncryptedKeys();
        for (String key : allKeys) {
            if (key.startsWith(GateStorage.DB_GATE_KEY_PREFIX) ||
                    key.startsWith(LevelStorage.DB_LEVEL_KEY_PREFIX) ||
                    key.startsWith(MissionStorage.DB_MISSION_KEY_PREFIX) ||
                    key.startsWith(ScoreStorage.DB_SCORE_KEY_PREFIX) ||
                    key.startsWith(WorldStorage.DB_WORLD_KEY_PREFIX)) {
                KeyValueStorage.deleteKeyValue(key);
            }
        }
    }

    private static void addWorldObjectToWorlds(HashMap<String, JSONObject> worlds, JSONObject worldJSON) throws JSONException {
        String worldId = worldJSON.getString("itemId");
        worlds.put(worldId, worldJSON);

        JSONArray worldsArr = worldJSON.getJSONArray("worlds");
        for (int i=0; i<worldsArr.length(); i++) {
            JSONObject innerWorldJSON = worldsArr.getJSONObject(i);
            addWorldObjectToWorlds(worlds, innerWorldJSON);
        }
    }

    private static HashMap<String, JSONObject> getListFromWorlds(JSONObject model, String listName) {
        HashMap<String, JSONObject> resultHash = new HashMap<String, JSONObject>();

        try {
            HashMap<String, JSONObject> worldJSONs = getWorlds(model);

            for (JSONObject worldJSON : worldJSONs.values()) {
                JSONArray objectJSONs = worldJSON.getJSONArray(listName);
                for(int i=0; i<objectJSONs.length(); i++) {
                    JSONObject objectJSON = objectJSONs.getJSONObject(i);
                    String objectId = objectJSON.getString("itemId");
                    resultHash.put(objectId, objectJSON);
                }
            }
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "couldn't get " + listName + " from worldJSON. error: " + e.getLocalizedMessage());
        }

        return resultHash;
    }

    private static void findInternalLists(HashMap<String, JSONObject> objects, String[] listClasses, String listName) {
        try {
            List<String> classes = Arrays.asList(listClasses);
            for (JSONObject objectJSON : objects.values()) {
                findInternalLists(objects, classes, listName, objectJSON);
            }
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "couldn't get internal lists for " + listName + ". error: " + e.getLocalizedMessage());
        }
    }

    private static void findInternalLists(HashMap<String, JSONObject> objects, List<String> listClasses, String listName, JSONObject checkJSON) throws JSONException {
        if (listClasses.contains(checkJSON.getString("className"))) {
            JSONArray internalList = checkJSON.getJSONArray(listName);
            for (int i = 0; i < internalList.length(); i++) {
                JSONObject targetObject = internalList.getJSONObject(i);
                String itemId = targetObject.getString("itemId");
                objects.put(itemId, targetObject);
                findInternalLists(objects, listClasses, listName, targetObject);
            }
        }
    }

    private static void applyGatesStateToJSON(JSONObject modelJSON, JSONObject stateJSON) {
        JSONObject gatesStateJSON = new JSONObject();
        HashMap<String, JSONObject> gates = getGates(modelJSON);
        for (JSONObject gateJSON : gates.values()) {
            JSONObject gateValuesJSON = new JSONObject();
            try {
                String gateId = gateJSON.getString("itemId");
                gateValuesJSON.put("open", GateStorage.isOpen(gateId));

                gatesStateJSON.put(gateId, gateValuesJSON);
            }
            catch (JSONException e) {
                SoomlaUtils.LogDebug(TAG, "Unable to get Gates state: " + e.getLocalizedMessage());
            }
        }

        try {
            stateJSON.put("gates", gatesStateJSON);
        } catch (JSONException e) {
            SoomlaUtils.LogDebug(TAG, "Unable to set Gates state: " + e.getLocalizedMessage());
        }
    }

    private static void applyWorldsStateToJSON(JSONObject modelJSON, JSONObject stateJSON) {
        JSONObject worldsStateJSON = new JSONObject();
        JSONObject levelsStateJSON = new JSONObject();

        HashMap<String, JSONObject> worlds = getWorlds(modelJSON);
        for (JSONObject worldJSON : worlds.values()) {
            JSONObject worldValuesJSON = new JSONObject();
            try {
                String worldId = worldJSON.getString("itemId");
                worldValuesJSON.put("completed", WorldStorage.isCompleted(worldId));
                worldValuesJSON.put("assignedReward", WorldStorage.getAssignedReward(worldId));

                worldsStateJSON.put(worldId, worldValuesJSON);

                if (worldJSON.getString("className").equals("Level")) {
                    JSONObject levelValuesJSON = new JSONObject();
                    levelValuesJSON.put("started", LevelStorage.getTimesStarted(worldId));
                    levelValuesJSON.put("played", LevelStorage.getTimesPlayed(worldId));
                    levelValuesJSON.put("timesCompleted", LevelStorage.getTimesCompleted(worldId));
                    levelValuesJSON.put("slowest", LevelStorage.getSlowestDurationMillis(worldId));
                    levelValuesJSON.put("fastest", LevelStorage.getFastestDurationMillis(worldId));

                    levelsStateJSON.put(worldId, levelValuesJSON);
                }
            }
            catch (JSONException e) {
                SoomlaUtils.LogDebug(TAG, "Unable to get Worlds state: " + e.getLocalizedMessage());
            }
        }

        try {
            stateJSON.put("worlds", worldsStateJSON);
            stateJSON.put("levels", levelsStateJSON);
        } catch (JSONException e) {
            SoomlaUtils.LogDebug(TAG, "Unable to set Worlds state: " + e.getLocalizedMessage());
        }
    }

    private static void applyMissionsStateToJSON(JSONObject modelJSON, JSONObject stateJSON) {
        JSONObject missionsStateJSON = new JSONObject();
        HashMap<String, JSONObject> missions = getMissions(modelJSON);
        for (JSONObject missionJSON : missions.values()) {
            JSONObject missionValuesJSON = new JSONObject();
            try {
                String missionId = missionJSON.getString("itemId");
                missionValuesJSON.put("timesCompleted", MissionStorage.getTimesCompleted(missionId));

                missionsStateJSON.put(missionId, missionValuesJSON);
            }
            catch (JSONException e) {
                SoomlaUtils.LogDebug(TAG, "Unable to get Missions state: " + e.getLocalizedMessage());
            }
        }

        try {
            stateJSON.put("missions", missionsStateJSON);
        } catch (JSONException e) {
            SoomlaUtils.LogDebug(TAG, "Unable to set Missions state: " + e.getLocalizedMessage());
        }
    }

    private static void applyScoresStateToJSON(JSONObject modelJSON, JSONObject stateJSON) {
        JSONObject scoresStateJSON = new JSONObject();
        HashMap<String, JSONObject> scores = getScores(modelJSON);
        for (JSONObject scoreJSON : scores.values()) {
            JSONObject scoreValuesJSON = new JSONObject();
            try {
                String scoreId = scoreJSON.getString("itemId");
                scoreValuesJSON.put("latest", ScoreStorage.getLatestScore(scoreId));
                scoreValuesJSON.put("record", ScoreStorage.getRecordScore(scoreId));

                scoresStateJSON.put(scoreId, scoreValuesJSON);
            }
            catch (JSONException e) {
                SoomlaUtils.LogDebug(TAG, "Unable to get Scores state: " + e.getLocalizedMessage());
            }
        }

        try {
            stateJSON.put("scores", scoresStateJSON);
        } catch (JSONException e) {
            SoomlaUtils.LogDebug(TAG, "Unable to set Scores state: " + e.getLocalizedMessage());
        }
    }

    private interface IItemStateApplier {
        boolean applyState(String itemId, JSONObject itemValuesJSON);
    }

    private static boolean resetStateFromJSON(JSONObject state, String targetListName, IItemStateApplier stateApplier) {
        if (!state.has(targetListName)) {
            return true;
        }

        SoomlaUtils.LogDebug(TAG, "Resetting state for " + targetListName);

        try {
            JSONObject itemsJSON = state.getJSONObject(targetListName);
            Iterator keysIter = itemsJSON.keys();
            while (keysIter.hasNext()) {
                String itemId = (String) keysIter.next();
                JSONObject itemValuesJSON = itemsJSON.getJSONObject(itemId);
                if (!stateApplier.applyState(itemId, itemValuesJSON)) {
                    return false;
                }
            }
        }
        catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "Unable to set state for " + targetListName + ". error: " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    private static boolean resetGatesStateFromJSON(JSONObject state) {
        return resetStateFromJSON(state, "gates", new IItemStateApplier() {
            @Override
            public boolean applyState(String itemId, JSONObject itemValuesJSON) {
                if (itemValuesJSON.has("open")) {
                    try {
                        boolean openState = itemValuesJSON.getBoolean("open");
                        GateStorage.setOpen(itemId, openState, false);
                    } catch (JSONException e) {
                        SoomlaUtils.LogError(TAG, "Unable to set state for gate " + itemId + ". error: " + e.getLocalizedMessage());
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private static boolean resetWorldsStateFromJSON(JSONObject state) {
        boolean worldsApplyState =  resetStateFromJSON(state, "worlds", new IItemStateApplier() {
            @Override
            public boolean applyState(String itemId, JSONObject itemValuesJSON) {
                try {
                    if (itemValuesJSON.has("completed")) {
                        boolean completedState = itemValuesJSON.getBoolean("completed");
                        WorldStorage.setCompleted(itemId, completedState, false);
                    }

                    if (itemValuesJSON.has("assignedReward")) {
                        String assignedRewardId = itemValuesJSON.getString("assignedReward");
                        WorldStorage.setReward(itemId, assignedRewardId, false);
                    }
                } catch (JSONException e) {
                    SoomlaUtils.LogError(TAG, "Unable to set state for world " + itemId + ". error: " + e.getLocalizedMessage());
                    return false;
                }

                return true;
            }
        });

        boolean levelsApplyState =  resetStateFromJSON(state, "levels", new IItemStateApplier() {
            @Override
            public boolean applyState(String itemId, JSONObject itemValuesJSON) {
                try {
                    if (itemValuesJSON.has("started")) {
                        int timesStarted = itemValuesJSON.getInt("started");
                        LevelStorage.setTimesStarted(itemId, timesStarted);
                    }

                    if (itemValuesJSON.has("played")) {
                        int timesPlayed = itemValuesJSON.getInt("played");
                        LevelStorage.setTimesPlayed(itemId, timesPlayed);
                    }

                    if (itemValuesJSON.has("timesCompleted")) {
                        int timesCompleted = itemValuesJSON.getInt("timesCompleted");
                        LevelStorage.setTimesCompleted(itemId, timesCompleted);
                    }

                    if (itemValuesJSON.has("slowest")) {
                        long slowest = itemValuesJSON.getLong("slowest");
                        LevelStorage.setSlowestDurationMillis(itemId, slowest);
                    }

                    if (itemValuesJSON.has("fastest")) {
                        long fastest = itemValuesJSON.getLong("fastest");
                        LevelStorage.setFastestDurationMillis(itemId, fastest);
                    }
                } catch (JSONException e) {
                    SoomlaUtils.LogError(TAG, "Unable to set state for level " + itemId + ". error: " + e.getLocalizedMessage());
                    return false;
                }

                return true;
            }
        });

        return worldsApplyState && levelsApplyState;
    }

    private static boolean resetMissionsStateFromJSON(JSONObject state) {
        return resetStateFromJSON(state, "missions", new IItemStateApplier() {
            @Override
            public boolean applyState(String itemId, JSONObject itemValuesJSON) {
                try {
                    if (itemValuesJSON.has("timesCompleted")) {
                        int timesCompleted = itemValuesJSON.getInt("timesCompleted");
                        MissionStorage.setTimesCompleted(itemId, timesCompleted);
                    }
                } catch (JSONException e) {
                    SoomlaUtils.LogError(TAG, "Unable to set state for level " + itemId + ". error: " + e.getLocalizedMessage());
                    return false;
                }

                return true;
            }
        });
    }

    private static boolean resetScoresStateFromJSON(JSONObject state) {
        return resetStateFromJSON(state, "scores", new IItemStateApplier() {
            @Override
            public boolean applyState(String itemId, JSONObject itemValuesJSON) {
                try {
                    if (itemValuesJSON.has("latest")) {
                        double latestScore = itemValuesJSON.getInt("latest");
                        ScoreStorage.setLatestScore(itemId, latestScore);
                    }

                    if (itemValuesJSON.has("record")) {
                        double recordScore = itemValuesJSON.getInt("record");
                        ScoreStorage.setRecordScore(itemId, recordScore);
                    }
                } catch (JSONException e) {
                    SoomlaUtils.LogError(TAG, "Unable to set state for level " + itemId + ". error: " + e.getLocalizedMessage());
                    return false;
                }

                return true;
            }
        });
    }

    private static final String TAG = "SOOMLA LevelUp";
}
