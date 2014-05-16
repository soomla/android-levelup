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
import com.soomla.blueprint.data.RewardsStorage;
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by refaelos on 13/05/14.
 */
public class SequenceReward extends Reward {
    private static final String TAG = "SOOMLA BadgeReward";
    private List<Reward> mRewards;

    protected SequenceReward(String rewardId, String name, List<Reward> rewards) {
        super(rewardId, name);
        mRewards = rewards;
    }

    public SequenceReward(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        try {
            mRewards = new ArrayList<Reward>();
            JSONArray rewardsArr = jsonObject.getJSONArray(BPJSONConsts.BP_REWARDS);
            for(int i=0; i<rewardsArr.length(); i++) {
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

    public List<Reward> getRewards() {
        return mRewards;
    }

    public Reward getLastGivenReward() {
        int idx = RewardsStorage.getLastSeqIdxGiven(this);
        if (idx < 0) {
            return null;
        }
        return mRewards.get(idx);
    }

    public boolean hasMoreToGive() {
        return RewardsStorage.getLastSeqIdxGiven(this) < mRewards.size() ;
    }

    public boolean forceNextRewardToGive(Reward reward) {
        for(int i=0; i<mRewards.size(); i++) {
            if (mRewards.get(i).getRewardId().equals(reward.getRewardId())) {
                RewardsStorage.setLastSeqIdxGiven(this, i-1);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean giveInner() {
        int idx = RewardsStorage.getLastSeqIdxGiven(this);
        if (idx >= mRewards.size()) {
            return false; // all rewards in the sequence were given
        }
        RewardsStorage.setLastSeqIdxGiven(this, ++idx);
        return true;
    }
}
