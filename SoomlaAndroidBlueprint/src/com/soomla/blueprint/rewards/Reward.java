package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.RewardsStorage;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 13/05/14.
 */
public abstract class Reward {
    private static final String TAG = "SOOMLA Reward";
    private String mRewardId;
    private String mName;

    protected Reward(String rewardId, String name) {
        mRewardId = rewardId;
        mName = name;
    }

    public Reward(JSONObject jsonObject) throws JSONException {
        mRewardId = jsonObject.getString(BPJSONConsts.BP_REWARD_REWARDID);
        try{
            mName = jsonObject.getString(BPJSONConsts.BP_NAME);
        } catch (JSONException ignored) {}
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_REWARD_REWARDID, mRewardId);
            jsonObject.put(BPJSONConsts.BP_NAME, mName);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public String getRewardId() {
        return mRewardId;
    }

    public String getName() {
        return mName;
    }

    public void give() {
        if (giveInner()) {
            RewardsStorage.setRewardStatus(this, true);
        }
    }

    public void take() {
        RewardsStorage.setRewardStatus(this, false);
    }

    public boolean owned() {
        return RewardsStorage.isRewardGiven(this);
    }

    protected abstract boolean giveInner();
}

