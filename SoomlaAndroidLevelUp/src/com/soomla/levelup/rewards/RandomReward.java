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

package com.soomla.levelup.rewards;

import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A specific type of <code>Reward</code> that holds of list of other
 * rewards. When this reward is given, it randomly chooses a reward from
 * the list of rewards it internally holds.  For example: a user can earn a mystery box
 * reward (<code>RandomReward</code>, which in fact grants the user a random reward between a
 * "Mayor" badge (<code>BadgeReward</code>) and a speed boost (<code>VirtualItemReward</code>)
 *
 * Created by refaelos on 13/05/14.
 */
public class RandomReward extends Reward {

    public static final String TYPE_NAME = "random";

    /**
     * Constructor
     *
     * @param rewardId see parent
     * @param name see parent
     * @param rewards a list of rewards from which to choose the reward randomly
     */
    protected RandomReward(String rewardId, String name, List<Reward> rewards) {
        super(rewardId, name);
        mRewards = rewards;
        setRepeatable(true);
    }

    /**
     * Constructor.
     * Generates an instance of <code>RandomReward</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>RandomReward</code>.
     * @throws JSONException
     */
    public RandomReward(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mRewards = new ArrayList<Reward>();
        JSONArray rewardsArr = jsonObject.optJSONArray(BPJSONConsts.BP_REWARDS);
        if (rewardsArr == null) {
            StoreUtils.LogWarning(TAG, "reward has no meaning without children");
            rewardsArr = new JSONArray();
        }

        // Iterate over all rewards in the JSON array and for each one create
        // an instance according to the reward type
        for (int i = 0; i < rewardsArr.length(); i++) {
            JSONObject rewardJSON = rewardsArr.getJSONObject(i);
            Reward reward = Reward.fromJSONObject(rewardJSON);
            if (reward != null) {
                mRewards.add(reward);
            }
        }

        setRepeatable(true);
    }

    /**
     * Converts the current <code>RandomReward</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RandomReward</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray rewardsArr = new JSONArray();
            for (Reward reward : mRewards) {
                rewardsArr.put(reward.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_REWARDS, rewardsArr);
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Gives a random reward from the list of rewards.
     *
     * @return <code>true</code>
     */
    @Override
    protected boolean giveInner() {
        Random rand = new Random();
        int n = rand.nextInt(mRewards.size());
        final Reward randomReward = mRewards.get(n);
        randomReward.give();
        mLastGivenReward = randomReward;

        return true;
    }

    @Override
    protected boolean takeInner() {
        // for now is able to take only last given
        if(mLastGivenReward == null) {
            return false;
        }

        final boolean taken = mLastGivenReward.take();
        mLastGivenReward = null;

        return taken;
    }

    /** Setters and Getters **/

    public List<Reward> getRewards() {
        return mRewards;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA RandomReward";

    private List<Reward> mRewards;
    private Reward mLastGivenReward;
}
