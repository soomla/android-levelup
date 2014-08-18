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

import com.soomla.Schedule;
import com.soomla.SoomlaEntity;
import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.gates.Gate;
import com.soomla.rewards.Reward;
import com.soomla.util.JSONFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A mission is a task your users needs to complete in your game. Missions are usually associated
 * with rewards meaning that you can give your users something for completing them.
 * You can create missions and use them as single, independent, entities OR you can create a
 * <code>Challenge</code> to handle several missions and monitor their completion.
 */
public abstract class Mission extends SoomlaEntity<Mission> {

    /**
     * Constructor.
     *
     * @param missionId the mission's ID
     * @param name      the mission's name (something you might want to display on the screen).
     */
    public Mission(String missionId, String name) {
        this(missionId, name, null, null);
    }

    /**
     * Constructor.
     *
     * @param id      the mission's ID
     * @param name    the mission's name (something you might want to display on the screen).
     * @param rewards the rewards that you want to give your users on mission completion.
     */
    public Mission(String id, String name, List<Reward> rewards) {
        this(id, name, rewards, null, null);
    }

    protected Mission(String id, String name, Class gateClass, Object[] gateInitParams) {
        this(id, name, new ArrayList<Reward>(), gateClass, gateInitParams);
    }

    protected Mission(String id, String name, List<Reward> rewards, Class gateClass, Object[] gateInitParams) {
        super(name, "", id);
        this.mRewards = rewards;
        if (gateClass != null) {

            //
            // Find the proper constructor by matching argument
            // classes to the provided initialization parameters
            //
            try {
                Constructor matchedConstructor = null;
                Constructor[] allConstructors = gateClass.getDeclaredConstructors();
                for (Constructor ctor : allConstructors) {
                    Class<?>[] pType = ctor.getParameterTypes();

                    boolean match = true;

                    for (int i = 0; i < pType.length; i++) {
                        if (!pType[i].equals(gateInitParams[i].getClass())) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        matchedConstructor = ctor;
                        break;
                    }
                }

                if (matchedConstructor == null) {
                    throw new ClassNotFoundException();
                }

                this.mGate = (Gate) matchedConstructor.newInstance(gateInitParams);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        this.mSchedule = Schedule.AnyTimeOnce();
    }


    /**
     * Constructor.
     * Generates an instance of <code>Mission</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Mission</code>.
     * @throws JSONException
     */
    public Mission(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        this.mRewards = new ArrayList<Reward>();
        JSONArray rewardsArr = jsonObject.getJSONArray(JSONConsts.SOOM_REWARDS);
        for (int i = 0; i < rewardsArr.length(); i++) {
            JSONObject rewardJSON = rewardsArr.getJSONObject(i);
            Reward reward = Reward.fromJSONObject(rewardJSON);
            if (reward != null) {
                mRewards.add(reward);
            }
        }

        this.mGate = Gate.fromJSONObject(jsonObject.getJSONObject(LUJSONConsts.LU_GATE));
        if (jsonObject.has(JSONConsts.SOOM_SCHEDULE)) {
            this.mSchedule = new Schedule(jsonObject.getJSONObject(JSONConsts.SOOM_SCHEDULE));
        }
    }

    /**
     * For JNI purposes
     *
     * @param jsonString
     * @return a mission from a JSON string
     */
    public static Mission fromJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJSONObject(jsonObject);
        } catch (JSONException e) {
            return null;
        }
    }

    public static Mission fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, Mission.class.getPackage().getName());
    }

    /**
     * Converts the current <code>Mission</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Mission</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();

        try {
            JSONArray rewardsArr = new JSONArray();
            for (Reward reward : mRewards) {
                rewardsArr.put(reward.toJSONObject());
            }
            jsonObject.put(JSONConsts.SOOM_REWARDS, rewardsArr);

            jsonObject.put(LUJSONConsts.LU_GATE, mGate.toJSONObject());
            jsonObject.put(JSONConsts.SOOM_SCHEDULE, mSchedule.toJSONObject());

        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Setters and Getters *
     */

    public List<Reward> getRewards() {
        return mRewards;
    }


    /**
     * Private Members *
     */

    private static final String TAG = "SOOMLA Mission";

    private static JSONFactory<Mission> sJSONFactory = new JSONFactory<Mission>();

    private List<Reward> mRewards;
    private Schedule mSchedule;
    protected Gate mGate;

}
