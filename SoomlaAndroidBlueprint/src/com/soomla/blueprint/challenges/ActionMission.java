package com.soomla.blueprint.challenges;

import com.soomla.blueprint.rewards.Reward;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class ActionMission extends Mission {

    public ActionMission(String name, String missionId) {
        super(name, missionId);
    }

    public ActionMission(String missionId, String name, List<Reward> rewards) {
        super(missionId, name, rewards);
    }
}
