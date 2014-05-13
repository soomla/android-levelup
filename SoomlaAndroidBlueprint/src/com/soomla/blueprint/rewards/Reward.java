package com.soomla.blueprint.rewards;

/**
 * Created by refaelos on 13/05/14.
 */
public abstract class Reward {
    private String mRewardId;
    private String mName;

    public String getRewardId() {
        return mRewardId;
    }

    public String getName() {
        return mName;
    }

    public abstract void give();
}

