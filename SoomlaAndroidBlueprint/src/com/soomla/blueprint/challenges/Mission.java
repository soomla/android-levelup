package com.soomla.blueprint.challenges;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.MissionsStorage;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.BadgeReward;
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
 * Created by refaelos on 13/05/14.
 */
public abstract class Mission {
    private static final String TAG = "SOOMLA Mission";
    private String mMissionId;
    private String mName;
    private List<Reward> mRewards = new ArrayList<Reward>();

    public Mission(String name, String missionId) {
        mName = name;
        mMissionId = missionId;
        mRewards = new ArrayList<Reward>();
    }

    public Mission(String missionId, String name, List<Reward> rewards) {
        mMissionId = missionId;
        mName = name;
        mRewards = rewards;
    }

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
            } else {
                StoreUtils.LogError(TAG, "Unknown reward type: " + type);
            }
        }
    }

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

    public List<Reward> getRewards() {
        return mRewards;
    }

    public String getName() {
        return mName;
    }

    public String getMissionId() {
        return mMissionId;
    }

    public boolean isCompleted() {
        return MissionsStorage.isCompleted(this);
    }

    public void setCompleted(boolean completed) {
        MissionsStorage.setCompleted(this, completed);
        if (completed) {
            giveRewards();
        }
    }

    private void giveRewards() {
        for(Reward reward : mRewards) {
            reward.give();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Mission) {
            Mission toCompare = (Mission)o;
            return this.mMissionId.equals(toCompare.getMissionId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.mMissionId.hashCode();
    }
}
