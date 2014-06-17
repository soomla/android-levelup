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

package com.soomla.levelup.challenges;

import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.events.MissionCompletedEvent;
import com.soomla.rewards.Reward;
import com.soomla.store.StoreUtils;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A challenge is a specific type of <code>Mission</code> which holds a collection
 * of missions.  The user is required to complete all these missions in order to earn
 * the reward associated with the challenge.
 *
 * Created by refaelos on 13/05/14.
 */
public class Challenge extends Mission {

    public static final String TYPE_NAME = "challenge";

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param missions the list of missions included in this challenge
     */
    public Challenge(String missionId, String name, List<Mission> missions) {
        super(missionId, name);
        mMissions = missions;
    }

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param missions the list of missions included in this challenge
     * @param rewards see parent
     */
    public Challenge(String missionId, String name, List<Mission> missions, List<Reward> rewards) {
        super(missionId, name, rewards);
        mMissions = missions;
    }

    /**
     * Constructor
     * Generates an instance of <code>Challenge</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public Challenge(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mMissions = new ArrayList<Mission>();
        JSONArray missionsArr = jsonObject.getJSONArray(BPJSONConsts.BP_MISSIONS);

        // Iterate over all missions in the JSON array and for each one create
        // an instance according to the mission type
        for (int i=0; i<missionsArr.length(); i++) {
            JSONObject missionJSON = missionsArr.getJSONObject(i);
            Mission mission = Mission.fromJSONObject(missionJSON);
            if (mission != null) {
                mMissions.add(mission);
            }
        }
    }

    /**
     * Converts the current <code>Challenge</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Challenge</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray missionsArr = new JSONArray();
            for (Mission mission : mMissions) {
                missionsArr.put(mission.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_MISSIONS, missionsArr);
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Checks whether the challenge is completed, i.e. all its missions are completed.
     *
     * @return <code>true</code> if the challenge is completed, <code>false</code> otherwise
     */
    @Override
    public boolean isCompleted() {
        for (Mission mission : mMissions) {
            if (!mission.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handles mission completion events. Checks if all missions included
     * in the challenge are completed, and if so, sets the challenge as completed.
     *
     * @param missionCompletedEvent
     */
    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent missionCompletedEvent) {
        if (mMissions.contains(missionCompletedEvent.Mission)) {
            boolean completed = true;
            for (Mission mission : mMissions) {
                if (!mission.isCompleted()) {
                    completed = false;
                    break;
                }
            }

            if (completed) {
                setCompleted(true);
            }
        }
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA Challenge";

    private List<Mission> mMissions;
}
