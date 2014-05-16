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

package com.soomla.blueprint.challenges;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.MissionsStorage;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.BadgeReward;
import com.soomla.blueprint.rewards.RandomReward;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.blueprint.rewards.VirtualItemReward;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A Mission is a task your users needs to complete in your game. Missions are usually associated
 * with rewards meaning that you can give your users something for completing them.
 * You can create missions and use them as single, independent, entities OR you can create a
 * <code>Challenge</code> to handle several missions and monitor their completion.
 */
public abstract class Mission {

    /**
     * Constructor.
     *
     * @param name is the name of the mission (something you might want to display on the screen.
     * @param missionId is the id of the mission
     */
    public Mission(String name, String missionId) {
        mName = name;
        mMissionId = missionId;
        mRewards = new ArrayList<Reward>();
    }

    /**
     * Constructor.
     *
     * @param name is the name of the mission (something you might want to display on the screen.
     * @param missionId is the id of the mission.
     * @param rewards is the rewards that you want to give your users on mission completion.
     */
    public Mission(String missionId, String name, List<Reward> rewards) {
        mMissionId = missionId;
        mName = name;
        mRewards = rewards;
    }

    /**
     * Constructor.
     * Generates an instance of <code>Mission</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Mission</code>.
     * @throws JSONException
     */
    public Mission(JSONObject jsonObject) throws JSONException {
        mMissionId = jsonObject.getString(BPJSONConsts.BP_MISSION_MISSIONID);
        mName = jsonObject.getString(BPJSONConsts.BP_NAME);

        mRewards = new ArrayList<Reward>();
        JSONArray rewardsArr = jsonObject.getJSONArray(BPJSONConsts.BP_REWARDS);
        for (int i=0; i<rewardsArr.length(); i++) {
            JSONObject rewardJSON = rewardsArr.getJSONObject(i);
            String type = rewardJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("badge")) {
                mRewards.add(new BadgeReward(rewardJSON));
            } else if (type.equals("item")) {
                mRewards.add(new VirtualItemReward(rewardJSON));
            } else if (type.equals("random")) {
                mRewards.add(new RandomReward(rewardJSON));
            } else {
                StoreUtils.LogError(TAG, "Unknown reward type: " + type);
            }
        }
    }

    /**
     * Converts the current <code>Mission</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Mission</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_MISSION_MISSIONID, mMissionId);
            jsonObject.put(BPJSONConsts.BP_NAME, mName);
            JSONArray rewardsArr = new JSONArray();
            for (Reward reward : mRewards) {
                rewardsArr.put(reward.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_REWARDS, rewardsArr);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Check weather the current mission is completed or not.
     *
     * @return the completion status of the current mission.
     */
    public boolean isCompleted() {
        return MissionsStorage.isCompleted(this);
    }

    /**
     * Use this function to force the completion status of the mission.
     * The completion status will be saved to the database.
     * In case of a successful completion, the associated rewards will be given.
     *
     * @param completed is the completion status you want to set to the mission.
     */
    public void setCompleted(boolean completed) {
        MissionsStorage.setCompleted(this, completed);
        if (completed) {

            // The mission is completed, giving the rewards.
            for(Reward reward : mRewards) {
                reward.give();
            }
        }
    }

    /**
     * Checks if the given Object is equal to this Mission, by comparing the given object's
     * mission id with this <code>Mission</code>'s missionId.
     *
     * @param o the object to compare
     * @return true if the objects are equal, otherwise false
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

    private String mMissionId;
    private String mName;
    private List<Reward> mRewards;
}
