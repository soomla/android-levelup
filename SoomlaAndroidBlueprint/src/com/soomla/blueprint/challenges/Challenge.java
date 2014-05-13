package com.soomla.blueprint.challenges;

import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class Challenge extends Mission {
    private List<Mission> mMissions;

    public Challenge(String missionId, String name, List<Mission> missions) {
        super(name, missionId);
        mMissions = missions;
        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public Challenge(String missionId, String name, List<Mission> missions, List<Reward> rewards) {
        super(missionId, name, rewards);
        mMissions = missions;
        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    @Override
    public boolean isCompleted() {
        for (Mission mission : mMissions) {
            if (!mission.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent missionCompletedEvent) {
        if (mMissions.contains(missionCompletedEvent.getMission())) {
            boolean completed = true;
            for (Mission mission : mMissions) {
                if (!mission.isCompleted()) {
                    completed = false;
                    break;
                }
            }

            if (completed) {
                BusProvider.getInstance().unregister(this);
                setCompleted(true);
            }
        }
    }
}
