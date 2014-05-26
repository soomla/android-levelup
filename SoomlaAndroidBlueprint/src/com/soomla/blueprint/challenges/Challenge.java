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

package com.soomla.blueprint.challenges;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
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

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param missions the list of missions included in this challenge
     */
    public Challenge(String missionId, String name, List<Mission> missions) {
        super(name, missionId);
        mMissions = missions;
        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
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

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
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
            String type = missionJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("balance")) {
                mMissions.add(new BalanceMission(missionJSON));
            } else if (type.equals("record")) {
                mMissions.add(new RecordMission(missionJSON));
            } else if (type.equals("challenge")) {
                mMissions.add(new Challenge(missionJSON));
            } else {
                StoreUtils.LogError(TAG, "Unknown mission type: " + type);
            }
        }

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
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
            jsonObject.put(BPJSONConsts.BP_TYPE, "challenge");
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


    /** Private Members **/

    private static final String TAG = "SOOMLA Challenge";

    private List<Mission> mMissions;
}
