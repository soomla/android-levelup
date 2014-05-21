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

package com.soomla.blueprint;

import com.soomla.blueprint.challenges.Challenge;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.LevelsStorage;
import com.soomla.blueprint.events.LevelEndedEvent;
import com.soomla.blueprint.events.LevelStartedEvent;
import com.soomla.blueprint.gates.GatesList;
import com.soomla.blueprint.scoring.Score;
import com.soomla.blueprint.scoring.VirtualItemScore;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * A level is specific type of <code>World</code> which can be started
 * and ended. During the level's game play, certain parameters are tracked and
 * saved such as level duration, score and number of times the level is played.
 *
 * Created by refaelos on 07/05/14.
 */
public class Level extends World {

    /**
     * Constructor
     *
     * @param worldId see parent
     */
    public Level(String worldId) {
        super(worldId);
    }

    /**
     * Constructor
     *
     * @param worldId see parent
     * @param gates see parent
     * @param scores see parent
     * @param challenges see parent
     */
    public Level(String worldId, GatesList gates, HashMap<String, Score> scores, List<Challenge> challenges) {
        super(worldId, gates, new HashMap<String, World>(), scores, challenges);
    }

    /**
     * Constructor
     *
     * @param worldId see parent
     * @param gates see parent
     * @param innerWorlds see parent
     * @param scores see parent
     * @param challenges see parent
     */
    public Level(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Challenge> challenges) {
        super(worldId, gates, innerWorlds, scores, challenges);
    }

    /**
     * Constructor.
     * Generates an instance of <code>Level</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Level</code>.
     * @throws JSONException
     */
    public Level(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_TYPE, "level");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    public int getTimesStarted() {
        return LevelsStorage.getTimesStarted(this);
    }

    public int getTimesPlayed() {
        return LevelsStorage.getTimesPlayed(this);
    }

    public double getSlowestDuration() {
        return LevelsStorage.getSlowestDuration(this);
    }

    public double getFastestDuration() {
        return LevelsStorage.getFastestDuration(this);
    }

    public void decScore(String scoreId, double amount) {
        mScores.get(scoreId).dec(amount);
    }

    public void incScore(String scoreId, double amount) {
        mScores.get(scoreId).inc(amount);
    }

    /**
     * Starts the level.
     * Call this method when game play in a certain level is initiated
     * @return
     */
    public boolean start() {
        StoreUtils.LogDebug(TAG, "Starting level with worldId: " + getWorldId());

        if (!canStart()) {
            return false;
        }

        LevelsStorage.incTimesStarted(this);

        mStartTime = System.currentTimeMillis();

        // Notify level has started
        BusProvider.getInstance().post(new LevelStartedEvent(this));
        return true;
    }

    /**
     * Ends the level.  Performs calculations of level play duration
     * and updates player's scores.
     * Call this method when game play in a certain level has reached an end.
     */
    public void end() {

        // Count number of times this level was played
        LevelsStorage.incTimesPlayed(this);

        // Calulate the slowest \ fastest durations of level play
        long endTime = System.currentTimeMillis();
        double duration = (endTime - mStartTime) / 1000.0;

        if (duration > getSlowestDuration()) {
            LevelsStorage.setSlowestDuration(this, duration);
        }

        if (duration < getFastestDuration()) {
            LevelsStorage.setFastestDuration(this, duration);
        }

        for(Score score : mScores.values()) {
            if (score instanceof VirtualItemScore) { // giving the user the items he collected
                String associatedItemId = ((VirtualItemScore) score).getAssociatedItemId();
                try {
                    StoreInventory.giveVirtualItem(associatedItemId, (int) score.getTempScore());
                } catch (VirtualItemNotFoundException e) {
                    StoreUtils.LogError(TAG, "Couldn't find item associated with a given " +
                            "VirtualItemScore. itemId: " + associatedItemId);
                }
            }

            score.saveAndReset(); // resetting scores
        }

        // Notify level has ended
        BusProvider.getInstance().post(new LevelEndedEvent(this));
    }


    /** Private Members **/

    private static String TAG = "SOOMLA Level";

    private long mStartTime;
}
