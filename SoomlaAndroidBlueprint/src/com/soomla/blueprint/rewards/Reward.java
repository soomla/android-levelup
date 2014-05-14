package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.RewardsStorage;

/**
 * Created by refaelos on 13/05/14.
 */
public abstract class Reward {
    private String mRewardId;
    private String mName;

    protected Reward(String rewardId, String name) {
        mRewardId = rewardId;
        mName = name;
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

