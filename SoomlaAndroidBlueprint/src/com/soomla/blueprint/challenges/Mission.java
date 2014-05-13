package com.soomla.blueprint.challenges;

import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public abstract class Mission {
    private String mMissionId;
    private boolean mCompleted;
    private String mName;
    private List<Reward> mRewards;

    public Mission(String name, String missionId) {
        mName = name;
        mMissionId = missionId;
    }

    public Mission(String missionId, String name, List<Reward> rewards) {
        mMissionId = missionId;
        mName = name;
        mRewards = rewards;
    }

    public List<Reward> getReward() {
        return mRewards;
    }

    public String getName() {
        return mName;
    }

    public String getMissionId() {
        return mMissionId;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean completed) {
        mCompleted = completed;
        if (completed) {
            giveRewards();
            BusProvider.getInstance().post(new MissionCompletedEvent(this));
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
