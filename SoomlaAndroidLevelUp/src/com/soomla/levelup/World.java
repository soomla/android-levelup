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

import android.text.TextUtils;

import com.soomla.SoomlaEntity;
import com.soomla.SoomlaUtils;
import com.soomla.levelup.challenges.Challenge;
import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.data.WorldStorage;
import com.soomla.levelup.gates.Gate;
import com.soomla.levelup.scoring.Score;
import com.soomla.rewards.Reward;
import com.soomla.util.JSONFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A world is the highest level entity in the LevelUp framework.  A world
 * is an entity that defines game progress and achievements with a set of
 * inner worlds (optional), challenges, missions, gates, scores and rewards.
 * A game can have several worlds that allow the user to progress between them.
 * An example can be observed in existing mobile games today such as Angry Birds:
 * <ul>
 * <li>A user plays in one world each time.</li>
 * <li>Each world contains multiple challenges which are associated with achieving
 * certain scores or badges.</li>
 * <li>Completing all challenges opens a gate to (=unlocks) the new world.</li>
 * <li>The user accumulates points to his \ her score.</li>
 * <li>Reaching a certain high score gives the user a reward - a virtual item,
 * a badge, a random reward etc.</li>
 * <li>A user can achieve new score records - "personal bests"</li>
 * </ul>
 * <p/>
 * Created by refaelos on 06/05/14.
 */
public class World extends SoomlaEntity<World> {

    /**
     * Constructor
     *
     * @param worldId the world's ID
     */
    public World(String worldId) {
        super("", "", worldId);
    }

    /**
     * Constructor
     *
     * @param id          see parent
     * @param gate        a gate
     * @param innerWorlds a map of worlds included in this world
     * @param scores      a map of scores used by this world
     * @param missions    a map of challenges used by this world
     */
    public World(String id, Gate gate, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Mission> missions) {
        super("", "", id);
        this.mInnerWorldsMap = innerWorlds;
        this.mScores = scores;
        this.mGate = gate;
        this.mMissions = missions;
    }

    /**
     * Constructor.
     * Generates an instance of <code>World</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>World</code>.
     * @throws JSONException
     */
    public World(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mInnerWorldsMap = new HashMap<String, World>();
        JSONArray worldsArr = jsonObject.getJSONArray(LUJSONConsts.LU_WORLDS);

        // Iterate over all inner worlds in the JSON array and for each one create
        // an instance according to the world type
        for (int i = 0; i < worldsArr.length(); i++) {
            JSONObject worldJSON = worldsArr.getJSONObject(i);
            World innerWorld = World.fromJSONObject(worldJSON);
            if (innerWorld != null) {
                mInnerWorldsMap.put(innerWorld.getID(), innerWorld);
            }
        }

        mScores = new LinkedHashMap<String, Score>();
        JSONArray scoresArr = jsonObject.getJSONArray(LUJSONConsts.LU_SCORES);

        // Iterate over all scores in the JSON array and for each one create
        // an instance according to the score type
        for (int i = 0; i < scoresArr.length(); i++) {
            JSONObject scoreJSON = scoresArr.getJSONObject(i);
            Score score = Score.fromJSONObject(scoreJSON);
            if (score != null) {
                mScores.put(score.getID(), score);
            }
        }

        mMissions = new ArrayList<Mission>();
        JSONArray missionsArr = jsonObject.getJSONArray(LUJSONConsts.LU_MISSIONS);

        // Iterate over all missions in the JSON array and create an instance for each one
        for (int i = 0; i < missionsArr.length(); i++) {
            JSONObject missionJSON = missionsArr.getJSONObject(i);
            mMissions.add(new Challenge(missionJSON));
        }

        JSONObject gateJSON = jsonObject.getJSONObject(LUJSONConsts.LU_GATE);
        if (gateJSON != null) { // this means that there are no gates !
            mGate = null;
        } else {
            mGate = Gate.fromJSONObject(gateJSON);
        }
    }

    /**
     * For JNI purposes
     *
     * @param jsonString
     * @return a mission from a JSON string
     */
    public static World fromJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJSONObject(jsonObject);
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
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_GATE, (mGate == null ? new JSONObject() : mGate.toJSONObject()));

            JSONArray worldsArr = new JSONArray();
            for (World world : mInnerWorldsMap.values()) {
                worldsArr.put(world.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_WORLDS, worldsArr);

            JSONArray scoresArr = new JSONArray();
            for (Score score : mScores.values()) {
                scoresArr.put(score.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_SCORES, scoresArr);

            JSONArray missionsArr = new JSONArray();
            for (Mission mission : mMissions) {
                missionsArr.put(mission.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_MISSIONS, missionsArr);

        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Adds another inner world to this world.
     *
     * @param world the world to add
     */
    public void addInnerWorld(World world) {
        mInnerWorldsMap.put(world.getID(), world);
    }

    /**
     * Adds another mission to the world.
     *
     * @param mission the mission to add
     */
    public void addMission(Mission mission) {
        mMissions.add(mission);
    }

    /**
     * Adds another score to the world.
     *
     * @param score the score to add
     */
    public void addScore(Score score) {
        mScores.put(score.getID(), score);
    }


    /**
     * Automatic generation of levels. *
     */
    private String idForAutoGeneratedLevel(String id, int idx) {
        return id + "_level" + idx;
    }

    private String idForAutoGeneratedScore(String id, int idx) {
        return id + "_score" + idx;
    }

    private String idForAutoGeneratedGate(String id) {
        return id + "_gate";
    }

    private String idForAutoGeneratedMission(String id, int idx) {
        return id + "_mission" + idx;
    }


    public void batchAddLevelsWithTemplates(int numLevels, Gate gateTemplate, Score scoreTemplate, Mission missionTemplate) {
        List<Score> scoreTemplates = new ArrayList<Score>();
        if (scoreTemplate != null) {
            scoreTemplates.add(scoreTemplate);
        }
        List<Mission> missionTemplates = new ArrayList<Mission>();
        if (missionTemplate != null) {
            missionTemplates.add(missionTemplate);
        }

        batchAddLevelsWithTemplates(numLevels, gateTemplate, scoreTemplates, missionTemplates);
    }

    public void batchAddLevelsWithTemplates(int numLevels, Gate gateTemplate, List<Score> scoreTemplates, List<Mission> missionTemplates) {
        for (int i = 0; i < numLevels; i++) {
            String lvlId = idForAutoGeneratedLevel(mID, i);
            Level aLvl = new Level(lvlId);

            aLvl.setGate(gateTemplate.clone(idForAutoGeneratedGate(lvlId)));

            if (scoreTemplates != null) {
                for (int k = 0; k < scoreTemplates.size(); k++) {
                    aLvl.addScore(scoreTemplates.get(k).clone(idForAutoGeneratedScore(lvlId, k)));
                }
            }

            if (missionTemplates != null) {
                for (int k = 0; i < missionTemplates.size(); k++) {
                    aLvl.addMission(missionTemplates.get(k).clone(idForAutoGeneratedMission(lvlId, k)));
                }
            }

            this.mInnerWorldsMap.put(lvlId, aLvl);
        }
    }


    /**
     * For Single Score *
     */

    public void setSingleScoreValue(double amount) {
        if (mScores.size() == 0) {
            return;
        }
        Score firstScore = mScores.entrySet().iterator().next().getValue();
        setScoreValue(firstScore.getID(), amount);
    }

    public void decSingleScore(double amount) {
        if (mScores.size() == 0) {
            return;
        }
        Score firstScore = mScores.entrySet().iterator().next().getValue();
        decScore(firstScore.getID(), amount);
    }

    public void incSingleScore(double amount) {
        if (mScores.size() == 0) {
            return;
        }
        Score firstScore = mScores.entrySet().iterator().next().getValue();
        incScore(firstScore.getID(), amount);
    }

    public Score getSingleScore() {
        if (mScores.size() == 0) {
            return null;
        }
        Score firstScore = mScores.entrySet().iterator().next().getValue();
        return firstScore;
    }

    public double sumInnerWorldsRecords() {
        double ret = 0;
        for (World world : getInnerWorldsList()) {
            ret += world.getSingleScore().getRecord();
        }
        return ret;
    }


    /**
     * For more than one Score *
     */

    public void ResetScores(boolean save) {
        if (mScores == null || mScores.isEmpty()) {
            SoomlaUtils.LogError(TAG, "(ResetScores) You don't have any scores defined in this world. World id: " + mID);
            return;
        }

        for (Score score : mScores.values()) {
            score.reset(save);
        }
    }

    public void decScore(String scoreId, double amount) {
        mScores.get(scoreId).dec(amount);
    }

    public void incScore(String scoreId, double amount) {
        mScores.get(scoreId).inc(amount);
    }

    /**
     * Retrieves all score records in the world.
     *
     * @return a map of {score ID: record}
     */
    public HashMap<String, Double> getRecordScores() {
        HashMap<String, Double> records = new HashMap<String, Double>();
        for (Score score : mScores.values()) {
            records.put(score.getID(), score.getRecord());
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
        for (Score score : mScores.values()) {
            latest.put(score.getID(), score.getLatest());
        }

        return latest;
    }

    /**
     * Sets a value for a certain score in the current game session.
     *
     * @param id       the ID of the score to set
     * @param scoreVal the value to set for the score
     */
    public void setScoreValue(String id, double scoreVal) {
        setScoreValue(id, scoreVal, false);
    }

    public void setScoreValue(String id, double scoreVal, boolean onlyIfBetter) {
        Score score = mScores.get(id);
        if (score == null) {
            SoomlaUtils.LogError(TAG, "(setScore) Can't find score id: " + id + "  world id: " + mID);
            return;
        }
        score.setTempScore(scoreVal, onlyIfBetter);
    }


    /** Completion **/

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
     * @param completed
     */
    public void setCompleted(boolean completed) {
        SetCompleted(completed, false);
    }

    public void SetCompleted(boolean completed, boolean recursive) {
        if (recursive) {
            for (World world : mInnerWorldsMap.values()) {
                world.setCompleted(completed, true);
            }
        }
        WorldStorage.setCompleted(this, completed);
    }


    /**
     * Reward Association *
     */

    public void assignReward(Reward reward) {
        String olderReward = getAssignedRewardId();
        if (!TextUtils.isEmpty(olderReward)) {
            Reward oldReward = LevelUp.getInstance().getReward(olderReward);
            if (oldReward != null) {
                oldReward.take();
            }
        }

        // We have to make sure the assigned reward can be assigned unlimited times.
        // There's no real reason why it won't be.
        if (reward.getSchedule().getActivationLimit() > 0) {
            reward.getSchedule().setActivationLimit(0);
        }

        reward.give();
        WorldStorage.setReward(this, reward.getID());
    }

    public String getAssignedRewardId() {
        return WorldStorage.getAssignedReward(this);
    }

    public boolean canStart() {
        return mGate == null || mGate.isOpen();
    }


    /**
     * Setters and Getters *
     */

    public void setGate(Gate gate) {
        mGate = gate;
    }

    public Gate getGate() {
        return mGate;
    }

    public Collection<World> getInnerWorldsList() {
        return mInnerWorldsMap.values();
    }

    public HashMap<String, World> getInnerWorlds() {
        return mInnerWorldsMap;
    }

    public HashMap<String, Score> getScores() {
        return mScores;
    }

    public List<Mission> getChallenges() {
        return mMissions;
    }


    /**
     * Private Members *
     */

    private static String TAG = "SOOMLA World";

    private static JSONFactory<World> sJSONFactory = new JSONFactory<World>();

    private Gate mGate;
    private HashMap<String, World> mInnerWorldsMap = new HashMap<String, World>();
    protected HashMap<String, Score> mScores = new LinkedHashMap<String, Score>();
    private List<Mission> mMissions = new ArrayList<Mission>();
}
