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
import com.soomla.levelup.data.RewardStorage;
import com.soomla.levelup.events.RewardTakenEvent;
import com.soomla.levelup.util.JSONFactory;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A reward is an entity which can be earned by the user for meeting certain
 * criteria in game progress.  For example - a user can earn a badge for completing
 * a mission. Dealing with <code>Reward</code>s is very similar to dealing with
 * <code>VirtualItem</code>s: grant a reward by giving it and recall a
 * reward by taking it.
 */
public abstract class Reward {

    /**
     * Constructor
     *
     * @param rewardId the reward's ID (unique id to distinguish between rewards).
     * @param name the reward's name.
     */
    protected Reward(String rewardId, String name) {
        mRewardId = rewardId;
        mName = name;
    }

    /**
     * Constructor.
     * Generates an instance of <code>Reward</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Reward</code>.
     * @throws JSONException
     */
    public Reward(JSONObject jsonObject) throws JSONException {
        mRewardId = jsonObject.getString(BPJSONConsts.BP_REWARD_REWARDID);
        mName = jsonObject.optString(BPJSONConsts.BP_NAME);
        mRepeatable = jsonObject.optBoolean(BPJSONConsts.BP_REWARD_REPEAT);
    }

    /**
     * Converts the current <code>Reward</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Reward</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_REWARD_REWARDID, mRewardId);
            jsonObject.put(BPJSONConsts.BP_NAME, mName);
            jsonObject.put(BPJSONConsts.BP_REWARD_REPEAT, mRepeatable);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public static Reward fromJSONObject(JSONObject jsonObject) {

        return sJSONFactory.create(jsonObject, sTypeMap);

//        if(jsonObject == null) {
//            StoreUtils.LogWarning(TAG, "fromJSONObject: jsonObject is null");
//            return null;
//        }
//
//        Reward reward = null;
//
//        try {
//            String type = jsonObject.getString(BPJSONConsts.BP_TYPE);
//            if (type.equals(BadgeReward.TYPE_NAME)) {
//                reward = new BadgeReward(jsonObject);
//            }
//            else if (type.equals(VirtualItemReward.TYPE_NAME)) {
//                reward = new VirtualItemReward(jsonObject);
//            }
//            else if (type.equals(RandomReward.TYPE_NAME)) {
//                reward = new RandomReward(jsonObject);
//            }
//            else if (type.equals(SequenceReward.TYPE_NAME)) {
//                reward = new SequenceReward(jsonObject);
//            }
//            else {
//                StoreUtils.LogError(TAG, "unknown reward type:" + type);
//            }
//        } catch (JSONException e) {
//            StoreUtils.LogError(TAG, "fromJSONObject JSONException:" + e.getMessage());
//        }
//
//        return reward;
    }

    /**
     * Grants this reward to the user. Use this method in cases where the user
     * has positive progress in game play and is eligible for earning this reward.
     * For example - give a reward when a user completes a mission or a challenge.
     *
     * @return if the reward was actually given
     */
    public boolean give() {
        if (RewardStorage.isRewardGiven(this) && !mRepeatable) {
            StoreUtils.LogDebug(TAG, "Reward was already given and is not repeatable. id: " + getRewardId());
            return false;
        }

        if (giveInner()) {
            RewardStorage.setRewardStatus(this, true);
            return true;
        }

        return false;
    }

    /**
     * Takes this reward from the user. Use this method in cases where the user
     * should be "punished", or has negative progress in the game
     * indicating that his \ her previously earned reward should be recalled.
     */
    public boolean take() {
        if (!RewardStorage.isRewardGiven(this)) {
            StoreUtils.LogDebug(TAG, "Reward not given. id: " + getRewardId());
            return false;
        }

        if (takeInner()) {
            RewardStorage.setRewardStatus(this, false);
            BusProvider.getInstance().post(new RewardTakenEvent(this));
            return true;
        }

        return false;
    }

    /**
     * Checks if the user owns this reward.
     *
     * @return <code>true</code> if owned, <code>false</code> otherwise
     */
    public boolean isOwned() {
        return RewardStorage.isRewardGiven(this);
    }

    /**
     * Tests the reward criteria which need to be met in order
     * to <code>give</code> the user this reward
     *
     * @return <code>true</code> if criteria is met, <code>false</code> otherwise
     */
    protected abstract boolean giveInner();

    /**
     * Tests the reward criteria which need to be met in order
     * to <code>take</code> the reward from the user
     * (not always possible?)
     *
     * @return <code>true</code> if criteria is met, <code>false</code> otherwise
     */
    protected abstract boolean takeInner();

    /** Setters and Getters **/

    public String getRewardId() {
        return mRewardId;
    }

    public String getName() {
        return mName;
    }

    public boolean isRepeatable() {
        return mRepeatable;
    }

    public void setRepeatable(boolean repeatable) {
        mRepeatable = repeatable;
    }

    /** Private Members **/

    private static final String TAG = "SOOMLA Reward";

    private static JSONFactory<Reward> sJSONFactory = new JSONFactory<Reward>();
    private static Map<String, Class<? extends Reward>> sTypeMap =
            new HashMap<String, Class<? extends Reward>>(4);
    static {
        sTypeMap.put(BadgeReward.TYPE_NAME, BadgeReward.class);
        sTypeMap.put(RandomReward.TYPE_NAME, RandomReward.class);
        sTypeMap.put(SequenceReward.TYPE_NAME, SequenceReward.class);
        sTypeMap.put(VirtualItemReward.TYPE_NAME, VirtualItemReward.class);
    }

    private String mRewardId;
    private String mName;
    private boolean mRepeatable = false;
}

