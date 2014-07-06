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

package com.soomla.levelup.challenges;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.data.MissionStorage;
import com.soomla.rewards.Reward;
import com.soomla.util.JSONFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A mission is a task your users needs to complete in your game. Missions are usually associated
 * with rewards meaning that you can give your users something for completing them.
 * You can create missions and use them as single, independent, entities OR you can create a
 * <code>Challenge</code> to handle several missions and monitor their completion.
 */
public abstract class Mission {

    /**
     * Constructor.
     *
     * @param missionId the mission's ID
     * @param name the mission's name (something you might want to display on the screen).
     */
    public Mission(String missionId, String name) {
        mName = name;
        mMissionId = missionId;
        mRewards = new ArrayList<Reward>();

        registerEvents();
    }

    /**
     * Constructor.
     *
     * @param missionId the mission's ID
     * @param name the mission's name (something you might want to display on the screen).
     * @param rewards the rewards that you want to give your users on mission completion.
     */
    public Mission(String missionId, String name, List<Reward> rewards) {
        mMissionId = missionId;
        mName = name;
        mRewards = rewards;

        registerEvents();
    }

    /**
     * Constructor.
     * Generates an instance of <code>Mission</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Mission</code>.
     * @throws JSONException
     */
    public Mission(JSONObject jsonObject) throws JSONException {
        mMissionId = jsonObject.getString(LUJSONConsts.LU_MISSION_MISSIONID);
        mName = jsonObject.getString(LUJSONConsts.LU_NAME);

        mRewards = new ArrayList<Reward>();
        JSONArray rewardsArr = jsonObject.getJSONArray(JSONConsts.SOOM_REWARDS);

        // Iterate over all missions in the JSON array and for each one create
        // an instance according to the mission type
        for (int i=0; i<rewardsArr.length(); i++) {
            JSONObject rewardJSON = rewardsArr.getJSONObject(i);
            Reward reward = Reward.fromJSONObject(rewardJSON);
            if (reward != null) {
                mRewards.add(reward);
            }
        }

        registerEvents();
    }

    public static Mission fromJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJSONObject(jsonObject);
        } catch (JSONException e) {
            return null;
        }
    }

    public static Mission fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, Mission.class.getPackage().getName());
    }

    /**
     * subscribe self to events in order to track
     * mission completion. Should be called on construction
     *
     * NOTE: override this and <code>unregisterEvents</code> to empty
     * if you need to use a <code>Mission</code> without events
     */
    protected void registerEvents() {
        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * unsubscribe self to events.
     * Should be called on setComplete(true)
     *
     * NOTE: see <code>registerEvents</code>
     */
    protected void unregisterEvents() {
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Converts the current <code>Mission</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Mission</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSONConsts.SOOM_CLASSNAME, getClass().getSimpleName());
            jsonObject.put(LUJSONConsts.LU_MISSION_MISSIONID, mMissionId);
            jsonObject.put(LUJSONConsts.LU_NAME, mName);
            JSONArray rewardsArr = new JSONArray();
            for (Reward reward : mRewards) {
                rewardsArr.put(reward.toJSONObject());
            }
            jsonObject.put(JSONConsts.SOOM_REWARDS, rewardsArr);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Check whether the current mission is completed or not.
     *
     * @return the completion status of the current mission.
     */
    public boolean isCompleted() {
        return MissionStorage.isCompleted(this);
    }

    /**
     * Forces the completion status of the mission.
     * The completion status will be saved to the database.
     * In case of a successful completion, the associated rewards will be given.
     *
     * @param completed the completion status you want to set to the mission.
     */
    public void setCompleted(boolean completed) {
        MissionStorage.setCompleted(this, completed);
        if (completed) {
            // events not interesting until revoked
            unregisterEvents();
            giveRewards();
        }
        else {
            takeRewards();
            // listen again for chance to be completed
            registerEvents();
        }
    }

    protected void takeRewards() {
        for (Reward reward : mRewards) {
            reward.take();
        }
    }

    protected void giveRewards() {
        // The mission is completed, giving the rewards.
        for(Reward reward : mRewards) {
            reward.give();
        }
    }

    /**
     * Checks if the given Object is equal to this mission, by comparing the given object's
     * <code>missionId</code> with this mission's <code>missionId</code>.
     *
     * @param o the object to compare
     * @return <code>true</code> if the objects are equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Mission) {
            Mission toCompare = (Mission)o;
            return this.mMissionId.equals(toCompare.getMissionId());
        }
        return false;
    }

    /**
     * Returns the hashCode of <code>mMissionId</code> if it exists
     *
     * @return the hashCode of <code>mMissionId</code>
     */
    @Override
    public int hashCode() {
        return this.mMissionId.hashCode();
    }


    /** Setters and Getters **/

    public String getMissionId() {
        return mMissionId;
    }

    public String getName() {
        return mName;
    }

    public List<Reward> getRewards() {
        return mRewards;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA Mission";

    private static JSONFactory<Mission> sJSONFactory = new JSONFactory<Mission>();

    private String mMissionId;
    private String mName;
    private List<Reward> mRewards;
}
