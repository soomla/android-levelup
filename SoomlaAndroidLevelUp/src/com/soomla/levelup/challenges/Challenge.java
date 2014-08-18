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

import com.soomla.SoomlaUtils;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.rewards.Reward;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A challenge is a specific type of <code>Mission</code> which holds a collection
 * of missions.  The user is required to complete all these missions in order to earn
 * the reward associated with the challenge.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class Challenge extends Mission {


    /**
     * Constructor
     *
     * @param id       see parent
     * @param name     see parent
     * @param missions the list of missions included in this challenge
     */
    public Challenge(String id, String name, List<Mission> missions) {
        super(id, name);
        mMissions = missions;
    }

    /**
     * Constructor
     *
     * @param id       see parent
     * @param name     see parent
     * @param missions the list of missions included in this challenge
     * @param rewards  see parent
     */
    public Challenge(String id, String name, List<Mission> missions, List<Reward> rewards) {
        super(id, name, rewards);
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

        JSONArray missionsArr = jsonObject.getJSONArray(LUJSONConsts.LU_MISSIONS);

        // Iterate over all missions in the JSON array and for each one create
        // an instance according to the mission type
        for (int i = 0; i < missionsArr.length(); i++) {
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
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray missionsArr = new JSONArray();
            for (Mission mission : mMissions) {
                missionsArr.put(mission.toJSONObject());
            }
            jsonObject.put(LUJSONConsts.LU_MISSIONS, missionsArr);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Private Members *
     */

    private static final String TAG = "SOOMLA Challenge";

    private List<Mission> mMissions = new ArrayList<Mission>();
}
