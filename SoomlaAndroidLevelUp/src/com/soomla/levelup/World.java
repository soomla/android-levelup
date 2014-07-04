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

import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;
import com.soomla.levelup.challenges.Challenge;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.data.WorldStorage;
import com.soomla.levelup.gates.Gate;
import com.soomla.levelup.gates.GatesList;
import com.soomla.levelup.gates.GatesListAND;
import com.soomla.levelup.scoring.Score;
import com.soomla.util.JSONFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A world is the highest level entity in the LevelUp framework.  A world
 * is an entity that defines game progress and achievements with a set of
 * inner worlds (optional), challenges, missions, gates, scores and rewards.
 * A game can have several worlds that allow the user to progress between them.
 * An example can be observed in existing mobile games today such as Angry Birds:
 * <ul>
 *     <li>A user plays in one world each time.</li>
 *     <li>Each world contains multiple challenges which are associated with achieving
 *     certain scores or badges.</li>
 *     <li>Completing all challenges opens a gate to (=unlocks) the new world.</li>
 *     <li>The user accumulates points to his \ her score.</li>
 *     <li>Reaching a certain high score gives the user a reward - a virtual item,
 *     a badge, a random reward etc.</li>
 *     <li>A user can achieve new score records - "personal bests"</li>
 * </ul>
 *
 * Created by refaelos on 06/05/14.
 */
public class World {

    /**
     * Constructor
     *
     * @param worldId the world's ID
     */
    public World(String worldId) {
        this.mWorldId = worldId;
        this.mGates = null;
        this.mInnerWorlds = new HashMap<String, World>();
        this.mScores = new HashMap<String, Score>();
        this.mChallenges = new ArrayList<Challenge>();
    }

    /**
     * Constructor
     *
     * @param worldId the world's ID
     * @param gates a list of gates that define
     * @param innerWorlds a map of worlds included in this world
     * @param scores a map of scores used by this world
     * @param challenges a map of challenges used by this world
     */
    public World(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Challenge> challenges) {
        this.mWorldId = worldId;
        this.mInnerWorlds = innerWorlds;
        this.mScores = scores;
        this.mGates = gates;
        this.mChallenges = challenges;
    }

    /**
     * Constructor.
     * Generates an instance of <code>World</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>World</code>.
     * @throws JSONException
     */
    public World(JSONObject jsonObject) throws JSONException {

        mWorldId = jsonObject.getString(LUJSONConsts.LU_WORLD_WORLDID);

        mInnerWorlds = new HashMap<String, World>();
        JSONArray worldsArr = jsonObject.getJSONArray(LUJSONConsts.LU_WORLDS);

        // Iterate over all inner worlds in the JSON array and for each one create
        // an instance according to the world type
        for (int i=0; i<worldsArr.length(); i++) {
            JSONObject worldJSON = worldsArr.getJSONObject(i);
            World innerWorld = World.fromJSONObject(worldJSON);
            if (innerWorld != null) {
                mInnerWorlds.put(innerWorld.getWorldId(), innerWorld);
            }
        }

        mScores = new HashMap<String, Score>();
        JSONArray scoresArr = jsonObject.getJSONArray(LUJSONConsts.LU_SCORES);

        // Iterate over all scores in the JSON array and for each one create
        // an instance according to the score type
        for (int i=0; i<scoresArr.length(); i++) {
            JSONObject scoreJSON = scoresArr.getJSONObject(i);
            Score score = Score.fromJSONObject(scoreJSON);
            if (score != null) {
                mScores.put(score.getScoreId(), score);
            }
        }

        mChallenges = new ArrayList<Challenge>();
        JSONArray challengesArr = jsonObject.getJSONArray(LUJSONConsts.LU_CHALLENGES);

        // Iterate over all challenges in the JSON array and create an instance for each one
        for (int i=0; i<challengesArr.length(); i++) {
            JSONObject challengesJSON = challengesArr.getJSONObject(i);
            mChallenges.add(new Challenge(challengesJSON));
        }

        JSONObject gateListJSON = jsonObject.getJSONObject(LUJSONConsts.LU_GATES);
        mGates = GatesList.fromJSONObject(gateListJSON);
    }

    public static World fromJSONString(String jsonString) {
        try {
            JSONObject rewardObj = new JSONObject(jsonString);
            return fromJSONObject(rewardObj);
        } catch (JSONException e) {
            return null;
        }
    }

    public static World fromJSONObject(JSONObject jsonObject) {

        return sJSONFactory.create(jsonObject, World.class.getPackage().getName());
    }


    /**
     * Converts the current <code>World</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>World</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSONConsts.SOOM_CLASSNAME, getClass().getSimpleName());
            jsonObject.put(LUJSONConsts.LU_WORLD_WORLDID, mWorldId);
            jsonObject.put(LUJSONConsts.LU_GATES, (mGates==null ? new JSONObject() : mGates.toJSONObject()));

            JSONArray worldsArr = new JSONArray();
            for (World world : mInnerWorlds.values()) {
                worldsArr.put(world.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_WORLDS, worldsArr);

            JSONArray scoresArr = new JSONArray();
            for (Score score : mScores.values()) {
                scoresArr.put(score.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_SCORES, scoresArr);

            JSONArray challengesArr = new JSONArray();
            for (Challenge challenge : mChallenges) {
                challengesArr.put(challenge.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_CHALLENGES, challengesArr);

        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Adds another challenge to the world.
     *
     * @param challenge the challenge to add
     */
    public void addChallenge(Challenge challenge) {
        mChallenges.add(challenge);
    }

    /**
     * Retrieves all score records in the world.
     *
     * @return a map of {score ID: record}
     */
    public HashMap<String, Double> getRecordScores() {
        HashMap<String, Double> records = new HashMap<String, Double>();
        for(Score score : mScores.values()) {
            records.put(score.getScoreId(), score.getRecord());
        }

        return records;
    }

    /**
     * Retrieves the most updated scores in the world.
     *
     * @return a map of {score ID: latest score}
     */
    public HashMap<String, Double> getLatestScores() {
        HashMap<String, Double> latest = new HashMap<String, Double>();
        for(Score score : mScores.values()) {
            latest.put(score.getScoreId(), score.getLatest());
        }

        return latest;
    }

    /**
     * Sets a value for a certain score in the current game session.
     *
     * @param scoreId the ID of the score to set
     * @param scoreVal the value to set for the score
     */
    public void setScore(String scoreId, double scoreVal) {
        Score score = mScores.get(scoreId);
        if (score == null) {
            SoomlaUtils.LogError(TAG, "(setScore) Can't find scoreId: " + scoreId + "  worldId: " + mWorldId);
            return;
        }
        score.setTempScore(scoreVal);
    }

    /**
     * Adds another score to the world.
     *
     * @param score the score to add
     */
    public void addScore(Score score) {
        mScores.put(score.getScoreId(), score);
    }

    /**
     * Adds another gate to the world.
     *
     * @param gate the gate to add
     */
    public void addGate(Gate gate) {
        if (mGates == null) {
            mGates = new GatesListAND(UUID.randomUUID().toString());
        }
        mGates.addGate(gate);
    }

    /**
     * Adds another inner world to this world.
     *
     * @param world the world to add
     */
    public void addInnerWorld(World world) {
        mInnerWorlds.put(world.getWorldId(), world);
    }

    /**
     * Checks if this world has been completed.
     *
     * @return <code>true</code> if completed, <code>false</code> otherwise
     */
    public boolean isCompleted() {
        return WorldStorage.isCompleted(this);
    }

    /**
     * Sets this world to be completed.
     *
     * @param mCompleted
     */
    public void setCompleted(boolean mCompleted) {
        setCompleted(mCompleted, false);
    }
    public void setCompleted(boolean completed, boolean recursive) {
        if (recursive) {
            for (World world : mInnerWorlds.values()) {
                world.setCompleted(completed, true);
            }
        }
        WorldStorage.setCompleted(this, completed);
    }

    /**
     * Checks if this world's starting criteria is met.
     * A world can be started if it has not gates or if
     * all of its gates are open.
     *
     * @return <code>true</code> if the world can be started,
     * <code>false</code> otherwise
     */
    public boolean canStart() {
        return mGates == null || mGates.isOpen();
    }


    /** Setters and Getters **/

    public String getWorldId() {
        return mWorldId;
    }

    public GatesList getGates() {
        return mGates;
    }

    public HashMap<String, World> getInnerWorlds() {
        return mInnerWorlds;
    }

    public HashMap<String, Score> getScores() {
        return mScores;
    }

    public List<Challenge> getChallenges() {
        return mChallenges;
    }


    /** Private Members **/

    private static String TAG = "SOOMLA World";

    private static JSONFactory<World> sJSONFactory = new JSONFactory<World>();

    private String mWorldId;
    private GatesList mGates;
    private HashMap<String, World> mInnerWorlds;
    protected HashMap<String, Score> mScores;
    private List<Challenge> mChallenges;
}
