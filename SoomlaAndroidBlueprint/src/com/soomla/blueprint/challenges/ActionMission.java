package com.soomla.blueprint.challenges;

import com.soomla.blueprint.rewards.Reward;

import java.util.List;

/**
 * A specific type of <code>Mission</code> which has no specific implementation
 * and allows the developer to define custom criteria for mission completion.
 * Override the {@link Mission#isCompleted()} in order to the define the custom criteria.
 *
 * Created by refaelos on 13/05/14.
 */
public class ActionMission extends Mission {

    /**
     * Constructor
     *
     * @param name see parent
     * @param missionId see parent
     */
    public ActionMission(String name, String missionId) {
        super(name, missionId);
    }

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param rewards see parent
     */
    public ActionMission(String missionId, String name, List<Reward> rewards) {
        super(missionId, name, rewards);
    }
}
