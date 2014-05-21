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

package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.BPJSONConsts;
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
        try {
            mRewards = new ArrayList<Reward>();
            JSONArray rewardsArr = jsonObject.getJSONArray(BPJSONConsts.BP_REWARDS);

            // Iterate over all rewards in the JSON array and for each one create
            // an instance according to the reward type
            for (int i = 0; i < rewardsArr.length(); i++) {
                JSONObject rewardJSON = rewardsArr.getJSONObject(i);
                String type = rewardJSON.getString(BPJSONConsts.BP_TYPE);
                if (type.equals("badge")) {
                    mRewards.add(new BadgeReward(rewardJSON));
                } else if (type.equals("item")) {
                    mRewards.add(new VirtualItemReward(rewardJSON));
                } else {
                    StoreUtils.LogError(TAG, "Unknown reward type: " + type);
                }
            }
        } catch (JSONException ignored) {}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray rewardsArr = new JSONArray();
            for (Reward reward : mRewards) {
                rewardsArr.put(reward.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_REWARDS, rewardsArr);
            jsonObject.put(BPJSONConsts.BP_TYPE, "random");
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
        int  n = rand.nextInt(mRewards.size());
        mRewards.get(n).give();
        return true;
    }


    /** Setters and Getters **/

    public List<Reward> getRewards() {
        return mRewards;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA BadgeReward";

    private List<Reward> mRewards;
}
